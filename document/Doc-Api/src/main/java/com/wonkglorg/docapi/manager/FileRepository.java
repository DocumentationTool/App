package com.wonkglorg.docapi.manager;

import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.git.GitRepo;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.ADDED;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.MODIFIED;
import static com.wonkglorg.docapi.git.GitRepo.GitStage.UNTRACKED;
import com.wonkglorg.docapi.git.RepoProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class FileRepository{
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
	
	public RepoProperties getRepoProperties() {
		return repoProperties;
	}
}
