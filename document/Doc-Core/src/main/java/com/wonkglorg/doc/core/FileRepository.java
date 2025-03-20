package com.wonkglorg.doc.core;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.InvalidTagException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.git.GitRepo;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.ADDED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.MODIFIED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.UNTRACKED;
import com.wonkglorg.doc.core.git.UserBranch;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.request.ResourceRequest;
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
import java.util.HashMap;
import java.util.HashSet;
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
public class FileRepository implements AutoCloseable{
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
	public void initialize() throws GitAPIException, CoreException, InvalidUserException {
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
			} catch(GitAPIException | CoreException | InvalidUserException e){
				log.error("Error while checking for changes", e);
			}
		}, 10, 10, TimeUnit.MINUTES);
		
	}
	
	private void checkFileChanges(Set<Path> foundFiles) throws CoreException, InvalidUserException {
		log.info("Checking for changes in {} files", foundFiles.size());
		
		ResourceRequest request = new ResourceRequest();
		request.targetPath(new TargetPath(null));
		request.repoId(repoProperties.getId());
		request.userId(null);
		
		List<Resource> resources = dataDB.getResources(request);
		
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
	 * Checks if a tag exists
	 *
	 * @param tags the tags to check
	 * @throws InvalidTagException if the tag does not exist
	 */
	public void checkTags(Set<TagId> tags) throws InvalidTagException {
		if(tags == null){
			return;
		}
		for(var tag : tags){
			if(!getDatabase().tagExists(tag)){
				throw new InvalidTagException("Tag '%s' does not exist in '%s'".formatted(tag, repoProperties.getId()));
			}
		}
	}
	
	/**
	 * Adds a file to the database
	 *
	 * @param resource the resource to add
	 */
	public void addResourceAndCommit(Resource resource) {
		try{
			UserBranch branch = gitRepo.createBranch(UserId.of(resource.createdBy()));
			Path file = gitRepo.getRepoPath().resolve(resource.resourcePath());
			if(!Files.exists(file)){
				Files.createDirectories(file.getParent());
				Files.createFile(file);
			}
			
			if(resource.data() != null){
				Files.write(file, resource.data().getBytes());
			}
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
	private void addNewFiles(List<Path> newFiles) throws CoreSqlException {
		List<Resource> resources = new ArrayList<>();
		for(Path file : newFiles){
			RevCommit lastCommitDetailsForFile = gitRepo.getLastCommitDetailsForFile(file.toString());
			Resource newResource;
			String content = readData(gitRepo, file);
			if(lastCommitDetailsForFile == null){
				log.error("File '{}' was not added by git", file);
				newResource = new Resource(file, "system", repoProperties.getId(), null, new HashSet<>(), content);
			} else {
				newResource = new Resource(file,
						lastCommitDetailsForFile.getAuthorIdent().getName(),
						repoProperties.getId(),
						null,
						new HashSet<>(),
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
	private int updateMatchingResources(List<Path> matchingResources, Map<Path, Resource> existingResources) throws CoreSqlException {
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
	
	private void deleteOldResources(List<Path> deletedResources) throws CoreSqlException {
		for(Path file : deletedResources){
			log.error("Deleting resource '{}'", file);
			gitRepo.remove(file);
		}
		dataDB.batchDelete(deletedResources);
	}
	
	public RepoProperty getRepoProperties() {
		return repoProperties;
	}

	@Override
	public void close() throws Exception {
		gitRepo.getGit().close();
		dataDB.close();

	}
}
