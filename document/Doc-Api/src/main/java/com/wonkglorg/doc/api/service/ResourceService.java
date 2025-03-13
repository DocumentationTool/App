package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidTagException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class ResourceService{
	
	private final RepoService repoService;
	
	public ResourceService(@Lazy RepoService repoService) {
		this.repoService = repoService;
	}
	
	/**
	 * Gets a repository by its id
	 *
	 * @param request the request
	 * @return the repository
	 * @throws Exception if the repository does not exist or null is passed
	 */
	public List<Resource> getResources(ResourceRequest request) throws CoreException {
		RepoId id = repoService.validateRepoId(request.repoId, true);
		
		if(id.isAllRepos()){ //gets resources from all repos
			List<Resource> allResources = new ArrayList<>();
			for(var repo : repoService.getRepositories().values()){
				allResources.addAll(repo.getDatabase().getResources(request));
			}
			return allResources;
		}
		
		return repoService.getRepo(id).getDatabase().getResources(request);
	}
	
	/**
	 * Checks if a resource exists
	 *
	 * @param repoId the repo id
	 * @param path the path
	 * @return true if the resource exists
	 */
	public boolean resourceExists(RepoId repoId, Path path) throws NotaRepoException {
		return repoService.getRepo(repoId).getDatabase().resourceExists(path).get();
	}
	
	/**
	 * Checks if a tag exists
	 *
	 * @param repoId the repo id
	 * @param tagId the tag id
	 * @return true if the tag exists
	 */
	public boolean tagExists(RepoId repoId, TagId tagId) {
		try{
			return repoService.getRepo(repoId).getDatabase().tagExists(tagId);
		} catch(NotaRepoException e){
			return false;
		}
	}
	
	public List<Tag> getTags(RepoId repoId, List<TagId> ids) throws InvalidTagException, NotaRepoException {
		List<Tag> tags = new ArrayList<>();
		FileRepository repo = repoService.getRepo(repoId);
		for(TagId id : ids){
			Tag tag = repo.getDatabase().getTagCache().get(id);
			if(tag == null){
				throw new InvalidTagException(repoId, "Tag '%s' does not exist".formatted(id));
			}
			tags.add(tag);
		}
		return tags;
	}
	
	/**
	 * Inserts a resource into the database
	 *
	 * @param resource the resource
	 * @return the response
	 */
	public void insertResource(Resource resource) throws ClientException {
		FileRepository repo = repoService.getRepo(resource.repoId());
		repo.getDatabase().insertResource(resource);
		repo.addResourceAndCommit(resource);
	}
	
	/**
	 * Updates a resource in the database
	 *
	 * @param repoId the repo id
	 * @param path the path
	 * @return the response
	 */
	public UpdateDatabaseResponse removeResource(RepoId repoId, Path path) {
		try{
			FileRepository repo = repoService.getRepo(repoId);
			UpdateDatabaseResponse updateDatabaseResponse = repo.getDatabase().removeResource(path);
			if(updateDatabaseResponse.isSuccess() && Files.exists(repo.getRepoProperties().getPath().resolve(path))){
				Files.delete((repo.getRepoProperties().getPath().resolve(path)));
			}
			return updateDatabaseResponse;
		} catch(NotaRepoException | IOException e){
			return UpdateDatabaseResponse.fail(null, e);
		}
	}
	
	/**
	 * Updates a resource in the database
	 *
	 * @param repoFrom the repo from
	 * @param pathFrom the path from
	 * @param repoTo the repo to
	 * * @param pathTo the path to
	 * @return the response
	 */
	public UpdateDatabaseResponse move(UserId id, RepoId repoFrom, Path pathFrom, RepoId repoTo, Path pathTo) {
		try{
			
			if(!repoService.isValidRepo(repoFrom) || !repoService.isValidRepo(repoTo)){
				return UpdateDatabaseResponse.fail(null, new NotaRepoException(repoFrom, "Repo does not exist"));
			}
			FileRepository fileRepoFrom = repoService.getRepo(repoFrom);
			FileRepository fileRepoTo = repoService.getRepo(repoTo);
			
			if(!resourceExists(repoFrom, pathFrom)){
				return UpdateDatabaseResponse.fail(null, new NotaRepoException(repoFrom, "Resource does not exist"));
			}
			
			if(resourceExists(repoTo, pathTo)){
				return UpdateDatabaseResponse.fail(null, new NotaRepoException(repoTo, "Resource already exists"));
			}
			
			ResourceRequest request = new ResourceRequest();
			request.path = pathFrom.toString();
			request.withData = true;
			request.repoId = repoFrom.toString();
			
			fileRepoFrom.removeResourceAndCommit(id, pathFrom);
			
			Resource resource = fileRepoFrom.getDatabase().getResources(request).get().stream().findFirst().orElseThrow();
			fileRepoTo.getDatabase().insertResource(resource);
			fileRepoTo.addResourceAndCommit(resource);
			return UpdateDatabaseResponse.success(repoTo, 1);
			
		} catch(NotaRepoException e){
			return UpdateDatabaseResponse.fail(null, e);
		}
	}
	
	/**
	 * Updates a resource in the database
	 *
	 * @param request the request
	 * @return the response
	 */
	public QueryDatabaseResponse<Resource> updateResource(ResourceUpdateRequest request) {
		try{
			FileRepository repo = repoService.getRepo(new RepoId(request.repoId));
			QueryDatabaseResponse<Resource> updateDatabaseResponse = repo.getDatabase().updateResourceData(request);
			if(updateDatabaseResponse.isSuccess()){
				repo.addResourceAndCommit(updateDatabaseResponse.get());
			}
			return updateDatabaseResponse;
		} catch(NotaRepoException e){
			return QueryDatabaseResponse.fail(null, e);
		}
	}
	
	/**
	 * Check if a file is currently being edited
	 *
	 * @param id the repo id
	 * @param path the path to check
	 * @return true if it is being edited, false otherwise
	 */
	public UserId getEditingUser(RepoId id, Path path) {
		return repoService.getRepo(id).getDatabase().isBeingEdited(path);
	}
	
	/**
	 * Check if a file is currently being edited
	 *
	 * @param id the repo id
	 * @param path the path to check
	 * @return true if it is being edited, false otherwise
	 */
	public boolean isBeingEdited(RepoId id, Path path) {
		return getEditingUser(id, path) != null;
	}
	
	/**
	 * Check if a user is currently editing a file
	 *
	 * @param userId the user to check
	 * @return true if they are editing, false otherwise
	 */
	public boolean isUserEditing(RepoId id, UserId userId) {
		return repoService.getRepo(id).getDatabase().isUserEditing(userId);
	}
	
	/**
	 * Sets a user as editing a file locking it for others to edit at the same time
	 *
	 * @param userId the user editing
	 * @param path the path to the file
	 * @return true if the file is now being edited, false otherwise
	 */
	public void setCurrentlyEdited(RepoId id, UserId userId, Path path) {
		repoService.getRepo(id).getDatabase().setCurrentlyEdited(userId, path);
	}
	
	/**
	 * Removes a user from editing a file
	 *
	 * @param userId the user to remove
	 */
	public void removeCurrentlyEdited(RepoId id, UserId userId) {
		repoService.getRepo(id).getDatabase().removeCurrentlyEdited(userId);
	}
	
	/**
	 * Removes a file from being edited
	 *
	 * @param path the path to the file
	 */
	public void removeCurrentlyEdited(RepoId id, Path path) {
		repoService.getRepo(id).getDatabase().removeCurrentlyEdited(path);
	}
}
