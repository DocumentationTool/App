package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.exception.NotaRepoException;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.apache.commons.lang3.NotImplementedException;
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
		
		if(!request.isSingleRepoRequest()){
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
			
			throw new NotImplementedException("Searching all repos not supported yet!");
		} else {
			RepoId id = new RepoId(repoId);
			if(!repoService.isValidRepo(id)){
				throw new NotaRepoException("Repo '%s' does not exist".formatted(repoId));
			}
			
			resources = repoService.getRepo(id).getDatabase().getResources(request);
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
	public boolean resourceExists(String repoId, Path path) {
		try{
			return repoService.getRepo(new RepoId(repoId)).getDatabase().resourceExists(path).get();
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
	public UpdateDatabaseResponse removeResource(String repoId, Path path) {
		try{
			UpdateDatabaseResponse updateDatabaseResponse = repoService.getRepo(new RepoId(repoId)).getDatabase().removeResource(path);
			if(updateDatabaseResponse.isSuccess()){
				Files.delete(repoService.getRepo(new RepoId(repoId)).getRepoProperties().getPath().resolve(path));
			}
			return updateDatabaseResponse;
		} catch(NotaRepoException | IOException e){
			return UpdateDatabaseResponse.fail(null, e);
		}
	}
	
}
