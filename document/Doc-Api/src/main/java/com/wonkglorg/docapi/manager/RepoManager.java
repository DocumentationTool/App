package com.wonkglorg.docapi.manager;

import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.db.objects.Resource;
import com.wonkglorg.docapi.git.GitRepo;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.ADDED;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.MODIFIED;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.UNTRACKED;
import com.wonkglorg.docapi.git.RepoProperties;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class RepoManager{
	/**
	 * A single repository that is being managed by the application
	 */
	public static class FileRepository{
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
		
		/**
		 * Initializes the repository by checking for the database file and updating the database
		 *
		 * @throws GitAPIException if there is an error with the git repo
		 */
		public void initialize() throws GitAPIException {
			log.info("Looking for repo in: '{}'", repoProperties.getPath());
			gitRepo = new GitRepo(repoProperties);
			Optional<Path> file = gitRepo.getSingleFile(s -> s.equalsIgnoreCase(repoProperties.getDbName()), UNTRACKED, MODIFIED, ADDED);
			
			if(file.isEmpty()){
				log.info("No Database in '{}'. Creating new Database.", repoProperties.getDbName());
			}
			
			dataDB = new RepoDB(repoProperties, gitRepo.getDatabaseRepoPath().resolve(repoProperties.getDbName()));
			dataDB.initialize();
			
			Set<Path> foundFiles = gitRepo.getFiles(s -> s.toLowerCase().endsWith(".xml"), UNTRACKED, MODIFIED, ADDED);
			
			boolean hasChanged = dataDB.updateResources(foundFiles);
			
			if(hasChanged && !repoProperties.isReadOnly()){
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
	//todo:jmd also cache all permissions, roles etc across all repos, how to best handle that?
	
	private final com.wonkglorg.docapi.properties.RepoProperties properties;
	
	public RepoManager(com.wonkglorg.docapi.properties.RepoProperties properties) {
		this.properties = properties;
	}
	
	public List<FileRepository> getRepositories() {
		return repositories;
	}
	
	@PostConstruct
	public void initialize() {
		log.info("Initializing RepoManager");
		
		for(RepoProperties repoProperty : properties.getRepositories()){
			log.info("Adding Repo '{}'", repoProperty.getName());
			FileRepository repository = new FileRepository(repoProperty);
			repositories.add(repository);
			
			// Initialize each repo asynchronously
			self.initializeRepositoryAsync(repository);
		}
	}

	
	// Asynchronous search across all repositories
	@Async
	public CompletableFuture<List<Resource>> searchAcrossRepositoriesAsync(String searchTerm) {
		// Perform async search for each repository
		List<CompletableFuture<List<Resource>>> searchFutures = repositories.stream().map(repo -> searchInRepositoryAsync(repo, searchTerm)).collect(
				Collectors.toList());
		
		// Combine all results once the searches complete
		return CompletableFuture.allOf(searchFutures.toArray(new CompletableFuture[0])).thenApply(v -> searchFutures.stream()
																													.flatMap(future -> future.join()
																																			 .stream()) // Flatten the results into a single list
																													.collect(Collectors.toList()));
	}

}
