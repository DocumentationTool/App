package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.git.GitRepo;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.ADDED;
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

@Component
public class RepoManager{
	private static final Logger log = LogManager.getLogger(RepoManager.class);
	private final List<FileRepository> repositories = new ArrayList<>();
	private final RepoProperties properties;
	
	public RepoManager(RepoProperties properties) {
		this.properties = properties;
	}
	
	@PostConstruct
	public void initialize() throws GitAPIException, IOException {
		log.info("Initializing RepoManager");
		
		for(RepoProperty repoProperty : properties.getRepositories()){
			log.info("Adding Repo '{}'", repoProperty.getName());
			repositories.add(new FileRepository(repoProperty));
		}
		
		for(FileRepository repository : repositories){
			log.info("Initializing Repo '{}'", repository.repoProperty.getName());
			repository.initialize();
		}
	}
	
	public List<FileRepository> getRepositories() {
		return repositories;
	}
	
	/**
	 * A single repository that is being managed by the application
	 */
	public static class FileRepository{
		private static final Logger log = LogManager.getLogger(FileRepository.class);
		private final RepoProperty repoProperty;
		/**
		 * The backing repo
		 */
		private GitRepo gitRepo;
		/**
		 * Represents the data in the database for quicker access and
		 */
		private DataDB dataDB;
		
		public FileRepository(RepoProperty repoProperty) {
			this.repoProperty = repoProperty;
		}
		
		public void initialize() throws GitAPIException, IOException {
			log.info("Looking for repo in: '{}'", repoProperty.getPath());
			gitRepo = new GitRepo(repoProperty.getPath());
			Optional<Path> file = gitRepo.getSingleFile(s -> s.equalsIgnoreCase(repoProperty.getDbName()), GitRepo.GitStage.UNTRACKED, ADDED, ADDED);
			dataDB = new DataDB(gitRepo.getRepoPath().resolve(repoProperty.getDbName()));
			if(file.isPresent()){
				//commit the file.
				log.info("No Database in Repo");
				gitRepo.getGit().add().addFilepattern(repoProperty.getDbName()).call();
			} else {
				log.info("Found database in repo!");
				//build db and then commit changes?
			}
			
			log.info("Initializing DataBase");
			dataDB.initialize();
		}
		
		public GitRepo getGitRepo() {
			return gitRepo;
		}
		
		public DataDB getDataDB() {
			return dataDB;
		}
	}
}
