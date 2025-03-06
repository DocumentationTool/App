package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles all api user specific requests
 */
@RestController
@RequestMapping(API_USER)
public class ApiUserController{
	private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);
	
	private final RepoService repoService;
	
	public ApiUserController(RepoService repoService) {
		this.repoService = repoService;
	}
	@Operation(
			summary = "Get a user's Roles",
			description = "Returns a list of Roles the user has. If a repository is given, only returns roles for that repository. If none is given, returns roles for all repositories."
	)
	@GetMapping("/get")
	public ResponseEntity<List<Role>> getUser(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("repoId") String repoId,
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("userId") String userId) {
		return null;
	}
	@Operation(
			summary = "Get a user's Roles",
			description = "Returns a list of Roles the user has. If a repository is given, only returns roles for that repository. If none is given, returns roles for all repositories."
	)
	@PutMapping("/add")
	public ResponseEntity<String> addUser(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("repoId") String repoId,
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("userId") String userId,
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("password") String password) {
		return null;
	}
	@Operation(
			summary = "Get a user's Roles",
			description = "Returns a list of Roles the user has. If a repository is given, only returns roles for that repository. If none is given, returns roles for all repositories."
	)
	@PutMapping("/remove")
	public ResponseEntity<Profile> deleteUser(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("repoId") String repoId,
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam("userId") String userId) {
		return null;
	}
}
