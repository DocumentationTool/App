package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_RESOURCE;
import com.wonkglorg.doc.api.json.JsonFileTree;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.api.service.ResourceService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//todo:jmd give descriptions to the rest endpoints

/**
 * Handles all api document specific requests
 */
@RestController
@RequestMapping(API_RESOURCE)
public class ApiResourceController{
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final ResourceService resourceService;
	private final RepoService repoService;
	
	public ApiResourceController(@Lazy RepoService repoService, ResourceService resourceService) {
		this.resourceService = resourceService;
		this.repoService = repoService;
	}
	
	//@formatter:off
	@Operation(summary = "Gets a resource", description = """
	## Returns resources by the specified request.
	
	### searchTerm
	if null is given returns the result regardless of its file contents.
	### path
	if null is given returns the result for all resources in the repository.
	### repoId
	if null is given returns the result for all repositories.
	### userId
	if null is given returns regardless of permissions, if a valid user is given limits the search to any file the user would have access to.
	### whiteListTags
	requires a resource to at least have this tag to be returned.
	### blacklistListTags
	requires a resource to not have this tag to be returned. If a resource has a whitelisted tag and a blacklisted tag it will not be returned as the blacklisted tag takes precedence.
	### withData
	if true returns the data of the resource.
	### returnLimit
	limits the amount of resources returned.
	""")
	@PostMapping("/get")
	public ResponseEntity<RestResponse<Map<String,List<JsonResource>>>> getResources(@RequestBody ResourceRequest request) {
		try{
			Collection<Resource> resources = getResourcesRequest(request);
			Map<String,List<JsonResource>> jsonResources = new HashMap<>();
			
			for(var resource: resources){
				jsonResources.computeIfAbsent(resource.repoId().id(),s ->new ArrayList<>()).add(JsonResource.of(resource));
			}
			return RestResponse.success(jsonResources).toResponse();
			
		}catch (Exception e) {
			return RestResponse.<Map<String,List<JsonResource>>>error(e.getMessage()).toResponse();
		}
	}
	
	private Collection<Resource> getResourcesRequest(ResourceRequest request) throws Exception {
			boolean isAntPath = pathMatcher.isPattern(request.path);
			String antPath = request.path;
			
			if(isAntPath){
				//match everything and and path match later (if optimisations are needed check for simple patterns to match)
				request.path = null;
			}
			
			Collection<Resource> resources = resourceService.getResources(request);
			if(isAntPath){
				resources = resources.stream().filter(r -> pathMatcher.match(antPath, r.resourcePath().toString())).toList();
			}
			
			return resources;
	}
	
	
	@Operation(summary = "Constructs a filetree", description = """
	## Constructs a file tree out of the given resource request.
	
	### searchTerm
	if null is given returns the result regardless of its file contents.
	### path
	if null is given returns the result for all resources in the repository.
	### repoId
	if null is given returns the result for all repositories.
	### userId
	if null is given returns regardless of permissions, if a valid user is given limits the search to any file the user would have access to.
	### whiteListTags
	requires a resource to at least have this tag to be returned.
	### blacklistListTags
	requires a resource to not have this tag to be returned. If a resource has a whitelisted tag and a blacklisted tag it will not be returned as the blacklisted tag takes precedence.
	### withData
	if true returns the data of the resource.
	### returnLimit
	limits the amount of resources returned.
	""")
	@PostMapping("/get/filetree")
	public ResponseEntity<RestResponse<Map<String, JsonFileTree>>> getFiletree(@RequestBody ResourceRequest request) {
		try {
			var resources = getResourcesRequest(request);
			Map<String, JsonFileTree> fileTrees = new HashMap<>();
			
			for (var resource : resources) {
				String repo = resource.repoId().id();
				String normalizedPath = resource.resourcePath().toString().replace("\\", "/");
				String[] pathSegments = normalizedPath.split("/");
				
				JsonFileTree root = fileTrees.computeIfAbsent(repo, JsonFileTree::new);
				
				//build file tree
				JsonFileTree current = root;
				for (int i = 0; i < pathSegments.length; i++) {
					String pathPart = pathSegments[i];
					
					if (i == pathSegments.length - 1 && pathPart.contains(".")) {
						// is a file
						current.addResource(resource);
					} else {
						//its a directory
						current = current.add(pathPart);
					}
				}
			}
			
			return RestResponse.success(fileTrees).toResponse();
			
		} catch (Exception e) {
			return RestResponse.<Map<String, JsonFileTree>>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Adds a resource", description = """
	## Body
	The data the file should contain.
	""")
	@PutMapping("/add")
	public ResponseEntity<RestResponse<Void>> insertResource(
			@Parameter(description = "The repoId to search in.")
			@RequestParam("repoId") String repoId,
			@Parameter(description = "The path to the resource.")
			@RequestParam("path") String path,
			@Parameter(description = "The user who created the resource.")
			@RequestParam("createdBy") String createdBy,
			@Parameter(description = "The category of the resource.")
			@RequestParam(value = "category",required = false) String category,
			@RequestBody String content) {
		
		RepoId id = new RepoId(repoId);
		if(!path.endsWith(".md")){
			path = path + ".md";
		}
		if(!repoService.isValidRepo(id)){
			return RestResponse.<Void>error("Repository does not exist").toResponse();
		}
			
		if(resourceService.resourceExists(new RepoId(repoId), Path.of(path))){
			return RestResponse.<Void>error("Resource already exists").toResponse();
		}
		
		Resource resource = new Resource(Path.of(path), createdBy, id, category, content);
		return RestResponse.of(resourceService.insertResource(resource)).toResponse();
	}
	
	//todo:jmd seperate those otherwise it gets messy
	
	//todo:jmd not sure how to implement this yet
	@Operation(summary = "Updates a resource", description = "Updates a resource in the Repository.")
	@PutMapping("/update")
	public ResponseEntity<RestResponse<Void>> updateResource(@RequestBody ResourceUpdateRequest request) {
		if(!repoService.isValidRepo(new RepoId(request.repoId))){
			return RestResponse.<Void>error("Repository does not exist").toResponse();
		}
		
		if(!resourceService.resourceExists(new RepoId(request.repoId), Path.of(request.path))){
			return RestResponse.<Void>error("Resource does not exist").toResponse();
		}
		
		
		QueryDatabaseResponse<Resource> response = resourceService.updateResource(request);
		if(response.isSuccess()){
			return RestResponse.<Void>success(response.getResponseText(),null).toResponse();
		}else{
			return RestResponse.<Void>error(response.getException().getMessage()).toResponse();
		}
	}
	
	
	@Operation(summary = "Removes a resource", description = "Removes a resource from the Repository.")
	@PostMapping("/remove")
	public ResponseEntity<RestResponse<Void>> removeResource(@RequestParam("repo") String repo,
															 @RequestParam("path") String path) {
		RepoId id = new RepoId(repo);
		if(!repoService.isValidRepo(id)){
			return RestResponse.<Void>error("Repository does not exist").toResponse();
		}
		if(!resourceService.resourceExists(id, Path.of(path))){
			return RestResponse.<Void>error("Resource does not exist").toResponse();
		}

		return RestResponse.of(resourceService.removeResource(id, Path.of(path))).toResponse();
	}
	
	
	@Operation(summary = "Moves a resource", description = "Moves a resource from one destination to another.")
	@PostMapping("/move")
	public ResponseEntity<RestResponse<Void>> moveResource (UserId userId,RepoId repoFrom, Path from, RepoId repoTo,Path to) {
		return RestResponse.of(resourceService.move(userId,repoFrom, from, repoTo, to)).toResponse();
	}
	
	
	//@formatter:on
}
