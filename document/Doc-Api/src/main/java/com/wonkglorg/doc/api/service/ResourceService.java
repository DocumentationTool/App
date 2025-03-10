package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.exception.InvalidTagException;
import com.wonkglorg.doc.api.exception.NotaRepoException;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Service
public class ResourceService{
	
	private final RepoService repoService;
	private final CacheManager cacheManager;
	
	public ResourceService(@Lazy RepoService repoService, CacheManager cacheManager) {
		this.repoService = repoService;
		this.cacheManager = cacheManager;
	}
	
	//DO NOT CACHE THIS METHOD, could contain a lot of data duo to the withData flag, it is already very optimized with internal resource caching in the getResources method
	
	/**
	 * Gets a repository by its id
	 *
	 * @param request the request
	 * @return the repository
	 * @throws Exception if the repository does not exist or null is passed
	 */
	public QueryDatabaseResponse<Collection<Resource>> getResources(ResourceRequest request) {
		if(request.repoId == null){ //gets resources from all repos
			QueryDatabaseResponse<Collection<Resource>> allResources = null;
			for(var repo : repoService.getRepositories().values()){
				//todo:jmd make em async later to improve performance
				QueryDatabaseResponse<Collection<Resource>> tempResponse = repo.getDatabase().getResources(request);
				if(tempResponse.isError()){
					return tempResponse;
				}
				if(allResources == null){
					allResources = tempResponse;
				} else {
					allResources.get().addAll(tempResponse.get());
				}
			}
			return allResources;
		}
		
		RepoId id = new RepoId(request.repoId);
		if(!repoService.isValidRepo(id)){
			return QueryDatabaseResponse.fail(null, new NotaRepoException("Repo '%s' does not exist".formatted(request.repoId)));
		}
		FileRepository repo;
		try{
			repo = repoService.getRepo(id);
		} catch(NotaRepoException e){
			return QueryDatabaseResponse.fail(null, e);
		}
		
		return repo.getDatabase().getResources(request);
	}
	
	/**
	 * Checks if a resource exists
	 *
	 * @param repoId the repo id
	 * @param path the path
	 * @return true if the resource exists
	 */
	public boolean resourceExists(RepoId repoId, Path path) {
		try{
			return repoService.getRepo(repoId).getDatabase().resourceExists(path).get();
		} catch(NotaRepoException e){
			return false;
		}
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
			Tag tag = repo.getDatabase().getTags().get(id);
			if(tag == null){
				throw new InvalidTagException("Tag '%s' does not exist".formatted(id));
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
	public UpdateDatabaseResponse insertResource(Resource resource) {
		try{
			FileRepository repo = repoService.getRepo(resource.repoId());
			UpdateDatabaseResponse updateDatabaseResponse = repo.getDatabase().insertResource(resource);
			if(updateDatabaseResponse.isSuccess()){
				repo.addResourceAndCommit(resource);
			}
			return updateDatabaseResponse;
		} catch(NotaRepoException e){
			return UpdateDatabaseResponse.fail(null, e);
		}
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
				return UpdateDatabaseResponse.fail(null, new NotaRepoException("Repo does not exist"));
			}
			FileRepository fileRepoFrom = repoService.getRepo(repoFrom);
			FileRepository fileRepoTo = repoService.getRepo(repoTo);
			
			if(!resourceExists(repoFrom, pathFrom)){
				return UpdateDatabaseResponse.fail(null, new NotaRepoException("Resource does not exist"));
			}
			
			if(resourceExists(repoTo, pathTo)){
				return UpdateDatabaseResponse.fail(null, new NotaRepoException("Resource already exists"));
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
	
}
