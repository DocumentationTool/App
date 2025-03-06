package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_RESOURCE;
import com.wonkglorg.doc.api.json.JsonFileTree;
import com.wonkglorg.doc.api.json.JsonRepos;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.service.RepoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//todo:jmd give descriptions to the rest endpoints

/**
 * Handles all api document specific requests
 */
@RestController
@RequestMapping(API_RESOURCE)
public class ApiResourceController{
	private static final Logger log = LoggerFactory.getLogger(ApiResourceController.class);
	private final RepoService repoService;
	
	public ApiResourceController(RepoService repoService) {
		this.repoService = repoService;
	}
	
	@Operation(summary = "Gets a resource", description = "Returns a resource or resources if no repository is given. If a repository is given, only resources for that repository will be returned, if no userId is given returns all resources in this repository without permission checks.")
	@GetMapping("/get")
	public ResponseEntity<RestResponse<JsonRepos<JsonResource>>> getResourcesByUser(
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
	
	@Operation(summary = "Constructs a filetree", description = "Returns a filetree or filetrees if no repository is given. If a repository is given, only filetrees for that repository will be returned, if no userId is given returns all filetrees in this repository without permission checks.")
	@GetMapping("/get/filetree")
	public ResponseEntity<RestResponse<JsonRepos<JsonFileTree>>> getFiletree(
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
}
