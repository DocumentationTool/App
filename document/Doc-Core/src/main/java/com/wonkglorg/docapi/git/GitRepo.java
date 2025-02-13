package com.wonkglorg.docapi.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.ServiceUnavailableException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a git repo
 */
public class GitRepo {
	public enum GitStage {
		UNTRACKED(Status::getUntracked),
		MODIFIED(Status::getModified),
		ADDED(Status::getAdded);
		private final Function<Status, Set<String>> getFiles;

		GitStage(Function<Status, Set<String>> getFiles) {
			this.getFiles = getFiles;
		}

		public Set<String> getFiles(Status stage) {
			return getFiles.apply(stage);
		}
	}

	private static final Logger log = LoggerFactory.getLogger(GitRepo.class);
	/**
	 * The Plumbing view of the backing git repo
	 * (https://git-scm.com/book/en/v2/Appendix-B:-Embedding-Git-in-your-Applications-JGit)
	 */
	private final Repository repository;
	/**
	 * The Porcelain view of the backing git repo
	 * (https://git-scm.com/book/en/v2/Appendix-B:-Embedding-Git-in-your-Applications-JGit)
	 */
	private final Git git;


	/**
	 * Is the same as {@link #repository} unless {@link RepoProperties#isReadOnly()} is defined, in
	 * that case this repo points specifically to the path defined by
	 * {@link RepoProperties#getDbStorage()}
	 */
	private final Repository databaseRepository;

	/**
	 * Behaves the same as {@link #databaseRepository}
	 */
	private final Git databaseGit;
	private RepoProperties properties;

	public GitRepo(RepoProperties properties) throws GitAPIException {
		this.properties = properties;
		Path pathToLocalRepo = properties.getPath();
		if (!Files.exists(pathToLocalRepo)) {
			log.info("No local repository found. Creating a new one...");
			if (!properties.isReadOnly()) {
				createRepoFromPath(pathToLocalRepo);
				repository = openRepoFromPath(pathToLocalRepo).orElseThrow();
				git = new Git(repository);
				log.info("Created a new git repository");
			} else {
				throw new ServiceUnavailableException(
						"Unable to locate Repository Marked as Read only! at path: " + pathToLocalRepo);
			}

			log.info("GitRepo initialized");
		} else {
			log.info("Local repository already exists");
			repository = openRepoFromPath(pathToLocalRepo).orElseThrow();
			git = new Git(repository);
			log.info("GitRepo opened");
		}
		Path pathToDB = properties.isReadOnly() ? properties.getDbStorage() : properties.getPath();

		if (!properties.isReadOnly()) {
			databaseRepository = repository;
			databaseGit = git;
			return;
		}

		if (properties.isReadOnly() && pathToDB == null) {
			throw new ServiceUnavailableException(
					"Read only repository with no valid database repository reference! For: %s".formatted(
							properties.getDbName()));
		}


		if (!Files.exists(pathToDB)) {
			log.info("No database repository found. Creating a new one...");
			createRepoFromPath(pathToLocalRepo);
			databaseRepository = openRepoFromPath(pathToLocalRepo).orElseThrow();
			databaseGit = new Git(repository);
			log.info("Created a new database git repository");
		} else {
			log.info("Local database repository already exists");
			databaseRepository = openRepoFromPath(pathToLocalRepo).orElseThrow();
			databaseGit = new Git(repository);
			log.info("GitRepo opened");
		}


	}

	/**
	 * Creates a git repository from the given path
	 *
	 * @param pathToRepo the path to the root of the repo
	 */
	public static void createRepoFromPath(Path pathToRepo) {
		File repoDir = pathToRepo.toFile();
		//creates the repo if it doesn't exist, FileRepository doesn't handle empty projects
		if (!repoDir.exists()) {
			try (Git ignored = Git.init().setDirectory(repoDir).call()) {
				log.info("Initialized new Git repository at: " + repoDir.getAbsolutePath());
			} catch (GitAPIException e) {
				log.error("Failed to initialize Git repository", e);
			}
		}
	}

	/**
	 * Opens an existing repo
	 *
	 * @param pathToRepo the path to the root of the repo
	 * @return an empty optional if no valid repo was fond, otherwise the loaded repo
	 */
	public static Optional<Repository> openRepoFromPath(Path pathToRepo) {

		try {
			return Optional.of(new FileRepositoryBuilder().setGitDir(
							pathToRepo.resolve(".git").toFile()) // Ensure it points to .git directory
					.readEnvironment().findGitDir().setMustExist(true).build());
		} catch (IOException e) {
			log.error("IO Exception while accessing repository at: " + pathToRepo, e);
		}
		return Optional.empty();
	}

	public void addFile(Path file) throws GitAPIException {
		git.add().addFilepattern(file.toString()).call();
	}

	public void addFile(String file) throws GitAPIException {
		git.add().addFilepattern(file).call();
	}

	public void commit(String message) throws GitAPIException {
		git.commit().setMessage(message).call();
	}

	/**
	 * Creates a new branch
	 *
	 * @param branchName the name of the branch
	 * @return the result of the creation
	 */
	public Ref createBranch(String branchName) {
		try {
			return git.branchCreate().setName(branchName).call();
		} catch (GitAPIException e) {
			log.error("Error while creating branch: " + branchName, e);
		}
		return null;
	}

	public List<String> deleteBranch(String branchName) {
		try {
			return git.branchDelete().setBranchNames(branchName).call();
		} catch (GitAPIException e) {
			log.error("Error while deleting branch '{}' for repo '{}'.", branchName,
					properties.getName(),
					e);
		}
		return new ArrayList<>();
	}

	/**
	 * @param filter
	 * @param stages
	 * @return the relative path to the repo
	 * @throws GitAPIException
	 */
	private HashSet<Path> get(Predicate<String> filter, boolean quitSingle, GitStage... stages)
			throws GitAPIException {
		HashSet<Path> files = new HashSet<>();
		Status status = git.status().call();
		Set<String> allFiles = new HashSet<>();
		for (GitStage stage : stages) {
			allFiles.addAll(stage.getFiles(status));
		}

		for (String filePath : allFiles) {
			if (filter.test(filePath)) {
				files.add(Path.of(filePath));
				if (quitSingle) {
					return files;
				}
			}
		}

		return files;
	}

	/**
	 * Retrieves files from git repo
	 *
	 * @param filter the filter the file path should match
	 * @param stages the stages the file could be in
	 * @return the relative path to the repo
	 * @throws GitAPIException
	 */
	public Set<Path> getFiles(Predicate<String> filter, GitStage... stages) throws GitAPIException {
		return get(filter, false, stages);
	}

	public Git getGit() {
		return git;
	}

	public RepoProperties getProperties() {
		return properties;
	}

	/**
	 * @return the working directory of the git repo
	 */
	public Path getRepoPath() {
		return Path.of(git.getRepository().getWorkTree().getAbsolutePath());
	}

	public Repository getRepository() {
		return repository;
	}


	/**
	 * @param filter
	 * @param stages
	 * @return the relative path to the repo
	 * @throws GitAPIException
	 */
	public Optional<Path> getSingleFile(Predicate<String> filter, GitStage... stages)
			throws GitAPIException {
		return get(filter, true, stages).stream().findFirst();
	}

	//todo:jmd create new branch anytime someone checks out a file and merge it after they are done
	// and want to save it
	//or trow away the branch if its not needed anymore and they quit the changes


	public Repository getDatabaseRepository() {
		return databaseRepository;
	}

	public Git getDatabaseGit() {
		return databaseGit;
	}
}
