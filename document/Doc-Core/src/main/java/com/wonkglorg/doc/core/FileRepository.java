package com.wonkglorg.doc.core;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.git.GitRepo;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.ADDED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.MODIFIED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.UNTRACKED;
import com.wonkglorg.doc.core.git.UserBranch;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents a managed repository
 */
public class FileRepository{
	private static final Logger log = LoggerFactory.getLogger(FileRepository.class);
	/**
	 * THe properties of the repository
	 */
	private final RepoProperty repoProperties;
	/**
	 * The backing repo
	 */
	private GitRepo gitRepo;
	/**
	 * Represents the backing database of a repo
	 */
	private RepositoryDatabase dataDB;
	private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
	
	public FileRepository(RepoProperty repoProperty) {
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
		
		Set<Path> foundFiles = gitRepo.getFiles(s -> s.toLowerCase().endsWith(".md"), UNTRACKED, MODIFIED, ADDED);
		
		checkFileChanges(foundFiles);
		
		log.info("Scheduling check for changes in '{}'", repoProperties.getId());
		executorService.scheduleAtFixedRate(() -> {
			try{
				log.info("Update task for repo '{}'", repoProperties.getId());
				checkFileChanges(gitRepo.getFiles(s -> s.toLowerCase().endsWith(".md"), UNTRACKED, MODIFIED, ADDED));
			} catch(GitAPIException e){
				log.error("Error while checking for changes", e);
			}
		}, 10, 10, TimeUnit.MINUTES);
		
	}
	
	private void checkFileChanges(Set<Path> foundFiles) {
		log.info("Checking for changes in {} files", foundFiles.size());
		
		ResourceRequest request = new ResourceRequest();
		request.path = null;
		request.repoId = repoProperties.getId().id();
		request.userId = null;
		
		QueryDatabaseResponse<Collection<Resource>> resourceRequest = dataDB.getResources(request);
		if(resourceRequest.isError()){
			log.error("Error while checking for changes: {}", resourceRequest.getErrorMessage());
			return;
		}
		
		Collection<Resource> resources = resourceRequest.get();
		Map<Path, Resource> resourceMap = resources.stream().collect(HashMap::new, (m, r) -> m.put(r.resourcePath(), r), Map::putAll);
		List<Path> newResources = foundFiles.stream().filter(f -> resources.stream().noneMatch(r -> r.resourcePath().equals(f))).toList();
		List<Path> deletedResources = resources.stream()
											   .map(Resource::resourcePath)
											   .filter(path -> foundFiles.stream().noneMatch(path::equals))
											   .toList();
		List<Path> matchingResources = resources.stream().map(Resource::resourcePath).filter(foundFiles::contains).toList();
		
		//pull any changes from the remote
		gitRepo.pull();
		
		int existingFilesChanged = updateMatchingResources(matchingResources, resourceMap);
		addNewFiles(newResources);
		deleteOldResources(deletedResources);
		log.info("--------Report for repo '{}--------", repoProperties.getId());
		if(newResources.isEmpty() && deletedResources.isEmpty() && existingFilesChanged == 0){
			log.info("No changes detected in repo '{}'", repoProperties.getId());
			log.info("--------End of report--------");
			return;
		} else {
			log.info("New resources: {}", newResources.size());
			log.info("Deleted resources: {}", deletedResources.size());
			log.info("Updated resources: {}", existingFilesChanged);
			log.info("--------End of report--------");
		}
		
		gitRepo.commit("Startup: Updated resources info: New: %s, Deleted: %s, Updated: %s".formatted(newResources.size(),
				deletedResources.size(),
				existingFilesChanged));
		gitRepo.push();
	}
	
	/**
	 * Adds a file to the database
	 *
	 * @param resource the resource to add
	 */
	public void addResourceAndCommit(Resource resource) {
		try{
			UserBranch branch = gitRepo.createBranch(new UserId(resource.createdBy()));
			Path resolve = gitRepo.getRepoPath().resolve(resource.resourcePath());
			Files.createDirectories(resolve.getParent());
			Path file = Files.createFile(resolve);
			Files.write(file, resource.data().getBytes());
			branch.addFile(file);
			branch.commit("Added resource %s".formatted(resource.resourcePath()));
			branch.closeBranch();
		} catch(IOException | GitAPIException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Removes a file from the database
	 *
	 * @param resourcePath the path to the resource
	 */
	public void removeResourceAndCommit(UserId userId, Path resourcePath) {
		try{
			UserBranch branch = gitRepo.createBranch(userId);
			branch.updateFileDeleted(resourcePath);
			branch.commit("Deleted resource %s".formatted(resourcePath));
			branch.closeBranch();
		} catch(GitAPIException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Adds new files to the database
	 *
	 * @param newFiles the files to add
	 */
	private void addNewFiles(List<Path> newFiles) {
		List<Resource> resources = new ArrayList<>();
		for(Path file : newFiles){
			RevCommit lastCommitDetailsForFile = gitRepo.getLastCommitDetailsForFile(file.toString());
			Resource newResource;
			String content = readData(gitRepo, file);
			if(lastCommitDetailsForFile == null){
				log.error("File '{}' was not added by git", file);
				newResource = new Resource(file, "system", repoProperties.getId(), null, new HashMap<>(), content);
			} else {
				newResource = new Resource(file,
						lastCommitDetailsForFile.getAuthorIdent().getName(),
						repoProperties.getId(),
						null,
						new HashMap<>(),
						content);
			}
			resources.add(newResource);
			gitRepo.add(file);
		}
		
		dataDB.batchInsert(resources);
	}
	
	/**
	 * Updates the resources in the database
	 *
	 * @param matchingResources the resources to update
	 * @return true if the resources have changed
	 */
	private int updateMatchingResources(List<Path> matchingResources, Map<Path, Resource> existingResources) {
		List<Resource> resources = new ArrayList<>();
		for(Path file : matchingResources){
			RevCommit fileCommit = gitRepo.getLastCommitDetailsForFile(file.toString());
			if(fileCommit == null){
				log.warn("Could not find commit for file '{}'", file);
				
				log.info("attempting to commit file '{}'", file);
				continue;
			}
			
			String authorName = fileCommit.getAuthorIdent().getName();
			Instant instant = Instant.ofEpochSecond(fileCommit.getCommitTime());
			LocalDateTime commitTime = LocalDateTime.ofInstant(instant, fileCommit.getAuthorIdent().getTimeZone().toZoneId());
			Resource existingResource = existingResources.get(file);
			
			//If the file has not been modified since the last commit, skip it
			if(existingResource.modifiedAt().isEqual(commitTime)){
				continue;
			}
			
			Resource newResource = new Resource(file,
					existingResource.createdAt(),
					existingResource.createdBy(),
					commitTime,
					authorName,
					repoProperties.getId(),
					existingResource.getResourceTags(),
					repoProperties.isReadOnly(),
					existingResource.category(),
					readData(gitRepo, file));
			resources.add(newResource);
			gitRepo.add(file);
		}
		dataDB.batchUpdate(resources);
		return resources.size();
	}
	
	private String readData(GitRepo gitRepo, Path file) {
		Path repoContextFile = gitRepo.getRepoPath().resolve(file);
		try{
			return Files.readString(repoContextFile);
		} catch(IOException e){
			log.error("Error while reading file data from '{}'", repoContextFile, e);
			return "";
		}
	}
	
	private void deleteOldResources(List<Path> deletedResources) {
		for(Path file : deletedResources){
			log.error("Deleting resource '{}'", file);
			gitRepo.remove(file);
		}
		dataDB.batchDelete(deletedResources);
	}
	
	public RepoProperty getRepoProperties() {
		return repoProperties;
	}
}
