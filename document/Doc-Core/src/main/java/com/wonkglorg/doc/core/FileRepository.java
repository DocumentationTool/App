package com.wonkglorg.doc.core;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.git.GitRepo;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.ADDED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.MODIFIED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.UNTRACKED;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a managed repository
 */
public class FileRepository{
	private static final Logger log = LoggerFactory.getLogger(FileRepository.class);
	/**
	 * THe properties of the repository
	 */
	private final RepoProperties repoProperties;
	/**
	 * The backing repo
	 */
	private GitRepo gitRepo;
	/**
	 * Represents the backing database of a repo
	 */
	private RepositoryDatabase dataDB;
	
	public FileRepository(RepoProperties repoProperty) {
		this.repoProperties = repoProperty;
	}
	
	public RepositoryDatabase getDatabase() {
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
		
		dataDB = new RepositoryDatabase(repoProperties, gitRepo.getDatabaseRepoPath().resolve(repoProperties.getDbName()));
		dataDB.initialize();
		
		Set<Path> foundFiles = gitRepo.getFiles(s -> s.toLowerCase().endsWith(".xml"), UNTRACKED, MODIFIED, ADDED);
		
		boolean hasChanged = dataDB.updateResources(foundFiles);
		
		if(hasChanged && !repoProperties.isReadOnly()){
			gitRepo.addFile(repoProperties.getDbName());
			gitRepo.commit("Updated File DB");
		}
	}
	
}
