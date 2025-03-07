package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonFileTree;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.request.ResourceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_RESOURCE;

//todo:jmd give descriptions to the rest endpoints

/**
 * Handles all api document specific requests
 */
@RestController
@RequestMapping(API_RESOURCE)
public class ApiResourceController {
    private final RepoService repoService;

    public ApiResourceController(RepoService repoService) {
        this.repoService = repoService;
    }

    //@formatter:off
	@Operation(summary = "Gets a resource", description = "Returns a resource or resources if no repository is given. If a repository is given, only resources for that repository will be returned, if no userId is given returns all resources in this repository without permission checks.")
	@PostMapping("/get")
	public ResponseEntity<RestResponse<Map<String,JsonResource>>> getResources(@RequestBody ResourceRequest request) {
		try{
			Map<String,JsonResource> jsonResources = new HashMap<>();
			List<Resource> resources = repoService.getResources(request);
			for(var resource: resources){
				jsonResources.put(resource.repoId().id(),JsonResource.of(resource));
			}
			return RestResponse.success(jsonResources).toResponse();

		}catch (Exception e) {
			return RestResponse.<Map<String,JsonResource>>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Constructs a filetree", description = "Returns a filetree or filetrees if no repository is given. If a repository is given, only filetrees for that repository will be returned, if no userId is given returns all filetrees in this repository without permission checks.")
	@GetMapping("/get/filetree")
	public ResponseEntity<RestResponse<Map<String,JsonFileTree>>> getFiletree(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam(value = "repoId", required = false) String repoId,
			@Parameter(description = "The userId to search for, if none is given, returns all resources in the repository.")
			@RequestParam(value = "userId", required = false) String userId,
			@Parameter(description = "The path to search for, if none is given, returns all resources in the repository.")
			@RequestParam(value = "path", required = false) String path,
			@Parameter(description = "If true only returns editable resources.")
			@RequestParam(value = "isEditable", required = false) Boolean isEditable) {
		return null;
	}
	
	@Operation(summary = "Adds a resource", description = "Adds a new resource to the Repository. ")
	@PutMapping("/add")
	public ResponseEntity<RestResponse<Void>> addResource(
			@RequestParam("repoId") String repoId,
			@RequestParam("path") String path,
			@RequestParam("createdBy") String createdBy,
			@RequestParam("category") String category,
			@RequestBody String content) {
		return null;
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
