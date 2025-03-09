package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.exception.NotaRepoException;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	@Cacheable("resources")
	public List<Resource> getResources(ResourceRequest request) throws Exception {
		String repoId = request.repoId;
		QueryDatabaseResponse<List<Resource>> resources = null;
		
		if(request.path == null){
			QueryDatabaseResponse<List<Resource>> tempResource;
			
			for(var repos : repoService.getRepositories().values()){
				tempResource = repos.getDatabase().getResources(request);
				if(resources == null){
					resources = tempResource;
				} else {
					if(tempResource.isError()){
						resources.setError(tempResource.getException());
					}
					resources.get().addAll(tempResource.get());
				}
			}
		} else {
			RepoId id = new RepoId(repoId);
			if(!repoService.isValidRepo(id)){
				throw new NotaRepoException("Repo '%s' does not exist".formatted(repoId));
			}
			
			resources = repoService.getRepo(id).getDatabase().getResources(request);
		}
		
		if(resources == null){
			return List.of();
		}
		
		if(resources.isError()){
			throw resources.getException();
		}
		
		return resources.get();
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
			
			Resource resource = fileRepoFrom.getDatabase().getResources(request).get().getFirst();
			fileRepoTo.getDatabase().insertResource(resource);
			fileRepoTo.addResourceAndCommit(resource);
			return UpdateDatabaseResponse.success(repoTo, 1);
			
		} catch(NotaRepoException e){
			return UpdateDatabaseResponse.fail(null, e);
		}
	}
	
	public UpdateDatabaseResponse updateResource(Resource resource) {
		try{
			FileRepository repo = repoService.getRepo(resource.repoId());
			UpdateDatabaseResponse updateDatabaseResponse = repo.getDatabase().updateResourceData(resource);
			if(updateDatabaseResponse.isSuccess()){
				repo.addResourceAndCommit(resource);
			}
			return updateDatabaseResponse;
		} catch(NotaRepoException e){
			return UpdateDatabaseResponse.fail(null, e);
		}
	}
	
}
