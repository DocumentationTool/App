package com.wonkglorg.docapi.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.ServiceUnavailableException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
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
	 * The Plumbing view of the backing git repo (https://git-scm.com/book/en/v2/Appendix-B:-Embedding-Git-in-your-Applications-JGit)
	 */
	private final Repository repository;
	/**
	 * The Porcelain view of the backing git repo (https://git-scm.com/book/en/v2/Appendix-B:-Embedding-Git-in-your-Applications-JGit)
	 */
	private final Git git;

	public GitRepo(Path pathToLocalRepo, boolean shouldCreate) throws GitAPIException, IOException {
		if (!Files.exists(pathToLocalRepo)) {
			if (shouldCreate) {
				repository = FileRepositoryBuilder.create(pathToLocalRepo.toFile());
				git = new Git(repository);
			} else {
				throw new ServiceUnavailableException(
						"Read only Git repository not found! at path: " + pathToLocalRepo);
			}

			log.info("GitRepo initialized");
		} else {
			repository = new FileRepositoryBuilder().setGitDir(pathToLocalRepo.toFile()).build();
			git = new Git(repository);
			log.info("GitRepo opened");
		}
	}

	public GitRepo(Repository repository) {
		this.repository = repository;
		this.git = new Git(repository);
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

	public Repository getRepository() {
		return repository;
	}

	/**
	 * @return the working directory of the git repo
	 */
	public Path getRepoPath() {
		return Path.of(git.getRepository().getWorkTree().getAbsolutePath());
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

}
