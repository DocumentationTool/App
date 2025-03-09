package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_RESOURCE;
import com.wonkglorg.doc.api.json.JsonFileTree;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.api.service.ResourceService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.request.ResourceRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
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
	
	public ApiResourceController(ResourceService resourceService, RepoService repoService) {
		this.resourceService = resourceService;
		this.repoService = repoService;
	}
	
	//@formatter:off
	@Operation(summary = "Gets a resource", description = "Returns a resource or resources if no repository is given. If a repository is given, only resources for that repository will be returned, if no userId is given returns all resources in this repository without permission checks.")
	@PostMapping("/get")
	public ResponseEntity<RestResponse<Map<String,List<JsonResource>>>> getResources(@RequestBody ResourceRequest request) {
		try{
			Map<String, List<JsonResource>> jsonResources = new HashMap<>();
			
			
			boolean isAntPath = pathMatcher.isPattern(request.path);
			
			if(isAntPath){
				//match everything and and path match later (if optimisations are needed check for simple patterns to match)
				request.path = null;
			}
			
			
			List<Resource> resources = resourceService.getResources(request);
			
			for(var resource: resources){
				jsonResources.computeIfAbsent(resource.repoId().id(),s ->new ArrayList<>()).add(JsonResource.of(resource));
			}
			
			if(isAntPath){
				for(var repo : jsonResources.entrySet()){
					repo.setValue(repo.getValue().stream().filter(r -> pathMatcher.match(request.path, r.path)).toList());
				}
			}
			
			return RestResponse.success(jsonResources).toResponse();

		}catch (Exception e) {
			return RestResponse.<Map<String,List<JsonResource>>>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Constructs a filetree", description = "Returns a filetree or filetrees if no repository is given. If a repository is given, only filetrees for that repository will be returned, if no userId is given returns all filetrees in this repository without permission checks.")
	@GetMapping("/get/filetree")
	public ResponseEntity<RestResponse<Map<String,JsonFileTree>>> getFiletree(@RequestBody ResourceRequest request) {
		return null;
	}
	
	@Operation(summary = "Adds a resource", description = "Adds a new resource to the Repository. ")
	@PutMapping("/add")
	public ResponseEntity<RestResponse<Void>> insertResource(
			@RequestParam("repoId") String repoId,
			@RequestParam("path") String path,
			@RequestParam("createdBy") String createdBy,
			@RequestParam(value = "category",required = false) String category,
			@RequestBody String content) {
		
		RepoId id = new RepoId(repoId);
		if(!repoService.isValidRepo(id)){
			return RestResponse.<Void>error("Repository does not exist").toResponse();
		}
			
		if(resourceService.resourceExists(repoId, Path.of(path))){
			return RestResponse.<Void>error("Resource already exists").toResponse();
		}
		
		Resource resource = new Resource(Path.of(path), createdBy, id, category, content);
		return RestResponse.of(resourceService.insertResource(resource)).toResponse();
	}
	
	//todo:jmd not sure how to implement this yet
	@Operation(summary = "Updates a resource", description = "Updates a resource in the Repository.")
	@PutMapping("/update")
	public ResponseEntity<RestResponse<Void>> updateResource(@RequestParam("repo") String repo,
															 @RequestParam("path") String path,
															 @RequestParam("createdBy") String createdBy,
															 @RequestParam("category") String category,
															 @RequestBody String content) {
		return null;
	}
	//@formatter:on
}
