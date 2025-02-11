package com.wonkglorg.docapi.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
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
	private final Git git;

	public GitRepo(Path pathToLocalRepo) throws GitAPIException, IOException {
		if (!Files.exists(pathToLocalRepo)) {
			git = Git.init().setDirectory(pathToLocalRepo.toFile()).call();
			log.info("GitRepo initialized");
		} else {
			git = Git.open(pathToLocalRepo.toFile());
			log.info("GitRepo opened");
		}
	}

	public GitRepo(Git git) {
		this.git = git;
	}

	/**
	 * @param filter
	 * @param stages
	 * @return
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
				files.add(Path.of(git.getRepository().getWorkTree().getAbsolutePath(), filePath));
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
	 * @param stage the stage the file could be in
	 * @return
	 * @throws GitAPIException
	 */
	public Set<Path> getFiles(Predicate<String> filter, GitStage... stages) throws GitAPIException {
		return get(filter, false, stages);
	}

	public Git getGit() {
		return git;
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
	 * @return
	 * @throws GitAPIException
	 */
	public Optional<Path> getSingleFile(Predicate<String> filter, GitStage... stages)
			throws GitAPIException {
		return get(filter, true, stages).stream().findFirst();
	}

}
