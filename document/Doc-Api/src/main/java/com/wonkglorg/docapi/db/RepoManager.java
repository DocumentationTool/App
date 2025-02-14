package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.db.objects.Resource;
import com.wonkglorg.docapi.git.GitRepo;
import com.wonkglorg.docapi.git.RepoProperties;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		private final RepoProperties repoProperties;
		/**
		 * The backing repo
		 */
		private GitRepo gitRepo;
		/**
		 * Represents the data in the database for quicker access and
		 */
		private RepoDB dataDB;

		public FileRepository(RepoProperties repoProperty) {
			this.repoProperties = repoProperty;
		}

		public RepoDB getDataDB() {
			return dataDB;
		}

		public GitRepo getGitRepo() {
			return gitRepo;
		}

		public void initialize() throws GitAPIException {
			log.info("Looking for repo in: '{}'", repoProperties.getPath());
			gitRepo = new GitRepo(repoProperties);
			Optional<Path> file =
					gitRepo.getSingleFile(s -> s.equalsIgnoreCase(repoProperties.getDbName()), UNTRACKED,
							MODIFIED, ADDED);

			if (file.isEmpty()) {
				log.info("No Database in '{}'. Creating new Database.", repoProperties.getDbName());
			}

			dataDB = new RepoDB(repoProperties,
					gitRepo.getDatabaseRepoPath().resolve(repoProperties.getDbName()));
			dataDB.initialize();


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
	/**
	 * A list of all loaded repositories
	 */
	private final List<FileRepository> repositories = new ArrayList<>();

	/**
	 * Keeps track of all cached resources for quick access in each repo
	 */
	private final Map<String, List<Resource>> cachedResources = new HashMap<>();

	private final com.wonkglorg.docapi.properties.RepoProperties properties;

	public RepoManager(com.wonkglorg.docapi.properties.RepoProperties properties) {
		this.properties = properties;
	}

	public List<FileRepository> getRepositories() {
		return repositories;
	}

	@PostConstruct
	public void initialize() throws GitAPIException, IOException {
		log.info("Initializing RepoManager");

		for (RepoProperties repoProperty : properties.getRepositories()) {
			log.info("Adding Repo '{}'", repoProperty.getName());
			repositories.add(new FileRepository(repoProperty));
		}

		for (FileRepository repository : repositories) {
			log.info("Initializing Repo '{}'", repository.repoProperties.getName());
			repository.initialize();
		}
	}
}
