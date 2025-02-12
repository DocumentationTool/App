package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.git.GitRepo;
import com.wonkglorg.docapi.properties.RepoProperties;
import com.wonkglorg.docapi.properties.RepoProperties.RepoProperty;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.wonkglorg.docapi.git.GitRepo.GitStage.*;

@Component
public class RepoManager {
	/**
	 * A single repository that is being managed by the application
	 */
	public static class FileRepository {
		private static final Logger log = LogManager.getLogger(FileRepository.class);
		private final RepoProperty repoProperties;
		/**
		 * The backing repo
		 */
		private GitRepo gitRepo;
		/**
		 * Represents the data in the database for quicker access and
		 */
		private DataDB dataDB;

		public FileRepository(RepoProperty repoProperty) {
			this.repoProperties = repoProperty;
		}

		public DataDB getDataDB() {
			return dataDB;
		}

		public GitRepo getGitRepo() {
			return gitRepo;
		}

		public void initialize() throws GitAPIException, IOException {
			log.info("Looking for repo in: '{}'", repoProperties.getPath());
			gitRepo = new GitRepo(repoProperties.getPath(), repoProperties.isReadOnly());
			Optional<Path> file =
					gitRepo.getSingleFile(s -> s.equalsIgnoreCase(repoProperties.getDbName()), UNTRACKED,
							MODIFIED, ADDED);
			dataDB = new DataDB(gitRepo.getRepoPath().resolve(repoProperties.getDbName()));
			if (file.isEmpty()) {
				log.info("No Database in Repo");
				dataDB.initialize();
			} else {
				log.info("Found database in repo!");
			}

			Set<Path> foundFiles =
					gitRepo.getFiles(s -> s.toLowerCase().endsWith(".xml"), UNTRACKED, MODIFIED, ADDED);

			boolean hasChanged = dataDB.updateResources(foundFiles);

			if (hasChanged && !repoProperties.isReadOnly()) {
				gitRepo.addFile(repoProperties.getDbName());
				gitRepo.commit("Updated File DB");
			}

		}
	}

	private static final Logger log = LogManager.getLogger(RepoManager.class);
	private final List<FileRepository> repositories = new ArrayList<>();
	private final RepoProperties properties;

	public RepoManager(RepoProperties properties) {
		this.properties = properties;
	}

	public List<FileRepository> getRepositories() {
		return repositories;
	}

	@PostConstruct
	public void initialize() throws GitAPIException, IOException {
		log.info("Initializing RepoManager");

		for (RepoProperty repoProperty : properties.getRepositories()) {
			log.info("Adding Repo '{}'", repoProperty.getName());
			repositories.add(new FileRepository(repoProperty));
		}

		for (FileRepository repository : repositories) {
			log.info("Initializing Repo '{}'", repository.repoProperties.getName());
			repository.initialize();
		}
	}
}
