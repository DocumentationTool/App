package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.properties.RepoProperties;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.RepoId;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all repositories (ALWAYS LAZY LOAD)
 */
@Component
@Service
public class RepoService{
	
	private static final Logger log = LogManager.getLogger(RepoService.class);
	/**
	 * A Map of all loaded repositories
	 */
	private final Map<RepoId, FileRepository> repositories = new HashMap<>();
	
	private final RepoProperties properties;
	private final UserService userService;
	
	public RepoService(RepoProperties properties, UserService userService) {
		this.properties = properties;
		this.userService = userService;
	}
	
	public Map<RepoId, FileRepository> getRepositories() {
		return repositories;
	}
	
	@PostConstruct
	public void initialize() {
		log.info("Initializing RepoService");
		repositories.clear();
		for(RepoProperty repoProperty : properties.getRepositories()){
			log.info("Adding Repo '{}'", repoProperty.getId());
			FileRepository repository = new FileRepository(repoProperty, userService.getUserDatabase());
			repositories.put(repoProperty.getId(), repository);
			try{
				repository.initialize();
			} catch(GitAPIException | CoreException | InvalidUserException e){
				log.error("Failed to initialize repository '{}'", repoProperty.getId(), e);
			}
		}
	}
	
	/**
	 * Gets a repository by its id
	 *
	 * @param repoId the id of the repository
	 * @return the repository
	 */
	public FileRepository getRepo(RepoId repoId) throws InvalidRepoException {
		validateRepoId(repoId);
		return repositories.get(repoId);
	}
	
	/**
	 * Gets all loaded repositories
	 *
	 * @return a list of all repositories
	 */
	public List<RepoProperty> getProperties() {
		return properties.getRepositories();
	}
	
	/**
	 * Checks if a repository is valid
	 *
	 * @param repoId the repo id to check
	 * @return true if the repo is valid
	 */
	public boolean isValidRepo(RepoId repoId) {
		if(repoId == null){
			return false;
		}
		return repositories.containsKey(repoId);
	}
	
	/**
	 * Validates if a repo id is valid, if null is given it will return the ALL_REPOS id
	 *
	 * @param repoId the repo id to validate
	 * @return the user id
	 */
	public RepoId validateRepoId(String repoId, boolean allowNull) throws InvalidRepoException {
		if(repoId == null && allowNull){
			return RepoId.ALL_REPOS;
		}
		
		if(repoId == null){
			throw new InvalidRepoException("Repo id is not allowed to be null!");
		}
		
		RepoId id = RepoId.of(repoId);
		if(!repositories.containsKey(id)){
			throw new InvalidRepoException("Repo '%s' does not exist".formatted(repoId));
		}
		
		return id;
	}
	
	/**
	 * Validates if a repo id is valid, if null is given throws an error
	 *
	 * @param repoId the repo id to validate
	 * @return the user id
	 */
	public RepoId validateRepoId(String repoId) throws InvalidRepoException {
		return validateRepoId(repoId, false);
	}
	
	public void validateRepoId(RepoId repoId) throws InvalidRepoException {
		if(!isValidRepo(repoId)){
			throw new InvalidRepoException("Repo '%s' does not exist".formatted(repoId));
		}
	}
}
