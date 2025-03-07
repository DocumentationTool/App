package com.wonkglorg.doc.core;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.git.GitRepo;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.ADDED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.MODIFIED;
import static com.wonkglorg.doc.core.git.GitRepo.GitStage.UNTRACKED;
import com.wonkglorg.doc.core.git.UserBranch;
import com.wonkglorg.doc.core.objects.Resource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private final RepoProperty repoProperties;
	/**
	 * The backing repo
	 */
	private GitRepo gitRepo;
	/**
	 * Represents the backing database of a repo
	 */
	private RepositoryDatabase dataDB;
	
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
	}
	
	private void checkFileChanges(Set<Path> foundFiles) {
		log.info("Checking for changes in {} files", foundFiles.size());
		
		ResourceRequest request = new ResourceRequest();
		request.path = null;
		request.repoId = repoProperties.getId().id();
		request.userId = null;
		
		QueryDatabaseResponse<List<Resource>> resourceRequest = dataDB.getResources(request);
		if(resourceRequest.isError()){
			log.error("Error while checking for changes: {}", resourceRequest.getErrorMessage());
			return;
		}
		
		List<Resource> resources = resourceRequest.get();
		Map<Path, Resource> resourceMap = resources.stream().collect(HashMap::new, (m, r) -> m.put(r.resourcePath(), r), Map::putAll);
		List<Path> newResources = foundFiles.stream().filter(f -> resources.stream().noneMatch(r -> r.resourcePath().equals(f))).toList();
		List<Path> deletedResources = resources.stream()
											   .map(Resource::resourcePath)
											   .filter(path -> foundFiles.stream().noneMatch(path::equals))
											   .toList();
		List<Path> matchingResources = resources.stream().map(Resource::resourcePath).filter(foundFiles::contains).toList();
		
		if(newResources.isEmpty() && deletedResources.isEmpty() && matchingResources.isEmpty()){
			log.info("No changes detected in repo '{}'", repoProperties.getId());
			return;
		}
		
		UserBranch branch = gitRepo.createBranch("system");
		
		int existingFilesChanged = updateMatchingResources(matchingResources, resourceMap, branch);
		addNewFiles(newResources, branch);
		deleteOldResources(deletedResources, branch);
		log.info("--------Report for repo '{}--------", repoProperties.getId());
		log.info("New resources: {}", newResources.size());
		log.info("Deleted resources: {}", deletedResources.size());
		log.info("Updated resources: {}", existingFilesChanged);
		log.info("--------End of report--------");
		
		branch.addFile(Path.of(repoProperties.getDbName()));
		branch.commit("Startup: Updated resources info: New: %s, Deleted: %s, Updated: %s".formatted(newResources.size(),
				deletedResources.size(),
				existingFilesChanged));
		try{
			branch.mergeIntoMain();
			branch.closeBranch();
		} catch(GitAPIException e){
			log.error("Error while merging branch into main", e);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Adds new files to the database
	 *
	 * @param newFiles the files to add
	 */
	private void addNewFiles(List<Path> newFiles, UserBranch branch) {
		List<Resource> resources = new ArrayList<>();
		for(Path file : newFiles){
			RevCommit lastCommitDetailsForFile = gitRepo.getLastCommitDetailsForFile(file.toString());
			Resource newResource;
			String content = readData(gitRepo, file);
			if(lastCommitDetailsForFile == null){
				log.error("File '{}' was not added by git", file);
				newResource = new Resource(file, "system", repoProperties.getId(), null, repoProperties.isReadOnly(), content);
			} else {
				String lastCommit = lastCommitDetailsForFile.getName();
				newResource = new Resource(file,
						lastCommitDetailsForFile.getAuthorIdent().getName(),
						repoProperties.getId(),
						lastCommit,
						repoProperties.isReadOnly(),
						content);
			}
			resources.add(newResource);
			branch.addFile(file);
		}
		
		dataDB.batchInsert(resources);
	}
	
	/**
	 * Updates the resources in the database
	 *
	 * @param matchingResources the resources to update
	 * @return true if the resources have changed
	 */
	private int updateMatchingResources(List<Path> matchingResources, Map<Path, Resource> existingResources, UserBranch branch) {
		List<Resource> resources = new ArrayList<>();
		for(Path file : matchingResources){
			RevCommit fileCommit = gitRepo.getLastCommitDetailsForFile(file.toString());
			if(fileCommit == null){
				log.warn("Could not find commit for file '{}'", file);
				
				log.info("attempting to commit file '{}'", file);
				continue;
			}
			
			String authorName = fileCommit.getAuthorIdent().getName();
			String lastCommit = fileCommit.getName();
			Instant instant = Instant.ofEpochSecond(fileCommit.getCommitTime());
			LocalDateTime commitTime = LocalDateTime.ofInstant(instant, fileCommit.getAuthorIdent().getTimeZone().toZoneId());
			
			Resource existingResource = existingResources.get(file);
			String currentCommit = existingResource.commitId();
			
			if(lastCommit.equals(currentCommit)){
				continue;
			}
			Resource newResource = new Resource(file,
					existingResource.createdAt(),
					existingResource.createdBy(),
					commitTime,
					authorName,
					repoProperties.getId(),
					existingResource.resourceTags(),
					lastCommit,
					repoProperties.isReadOnly(),
					existingResource.category(),
					readData(gitRepo, file));
			resources.add(newResource);
			branch.addFile(file);
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
	
	private void deleteOldResources(List<Path> deletedResources, UserBranch branch) {
		for(Path file : deletedResources){
			log.error("Deleting resource '{}'", file);
			branch.updateFileDeleted(file);
		}
		dataDB.batchDelete(deletedResources);
	}
	
	public RepoProperty getRepoProperties() {
		return repoProperties;
	}
}
