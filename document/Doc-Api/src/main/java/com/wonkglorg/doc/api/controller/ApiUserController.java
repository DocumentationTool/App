package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;
import com.wonkglorg.doc.api.json.JsonRepos;
import com.wonkglorg.doc.api.json.JsonUsers;
import com.wonkglorg.doc.api.service.RepoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles all api user specific requests
 */
@RestController
@RequestMapping(API_USER)
public class ApiUserController{
	
	private final RepoService repoService;
	
	public ApiUserController(RepoService repoService) {
		this.repoService = repoService;
	}
	
	//@formatter:off
	@Operation(
			summary = "Get a user",
			description = "Returns a user or users if no repository is given. If a repository is given, only returns users for that repository will be returned, if no userId is given returns all users in this repository."
	)
	@GetMapping("/get")
	public ResponseEntity<RestResponse<JsonRepos<JsonUsers>>> getUser(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam(value = "repoId",required = false) String repoId,
			@Parameter(description = "The userId to search for, if none is given, returns all users in the repository.")
			@RequestParam(value = "userId",required = false) String userId) {
		return null;
	}
	@Operation(
			summary = "Adds a new user",
			description = "Adds a new user to the system. If a repository is given, only adds the user to that repository. If none is given, adds the user to all repositories."
	)
	@PutMapping("/add")
	public ResponseEntity<String> addUser(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam(value = "repoId",required = false) String repoId,
			@Parameter(description = "The user's id.")
			@RequestParam("userId") String userId,
			@Parameter(description = "The user's password.")
			@RequestParam("password") String password) {
		return null;
	}
	@Operation(
			summary = "Removes a User",
			description = "Removes a user from the system."
	)
	@PutMapping("/remove")
	public ResponseEntity<Profile> deleteUser(
			@Parameter(description = "The repoId to search in.")
			@RequestParam("repoId") String repoId,
			@Parameter(description = "The users id to remove.")
			@RequestParam("userId") String userId) {
		return null;
	}
	//@formatter:on
}
