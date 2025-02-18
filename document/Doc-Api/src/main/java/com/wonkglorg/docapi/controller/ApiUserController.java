package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.manager.RepoManager;
import com.wonkglorg.docapi.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.wonkglorg.docapi.controller.Constants.ControllerPaths.API_USER;

/**
 * Handles all api user specific requests
 */
@Controller
@RequestMapping(API_USER)
public class ApiUserController {
	//todo, setup specific api key to bypass role permissions (when the app itself needs to make
	// changes / be setup for new admins)
	private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);
	
	private final RepoManager repoManager;
	
	public ApiUserController(RepoManager repoManager) {
		this.repoManager = repoManager;
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/add")
	public ResponseEntity<UserProfile> addUser(@RequestParam("userID") String userId,
			@RequestParam("password") String password) {
		log.info("Received PUT request to add user with ID='{}'. Password provided: {}", userId,
				password != null ? "*".repeat(password.length()) : "MISSING");
		return ResponseEntity.ok(null);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/remove")
	public ResponseEntity<UserProfile> deleteUser(@RequestParam("userID") String userId) {
		log.info("Received PUT request to delete user with userID='{}'", userId);
		return ResponseEntity.ok(null);
	}


	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<UserProfile> getUser(@PathVariable("id") String id) {
		log.info("Received GET request to retrieve user with userID='{}'", id);
		return ResponseEntity.ok(null);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/update")
	public ResponseEntity<UserProfile> updateUser(@RequestBody UserProfile userId) {
		log.info("Received PUT request to update user profile for userID='{}'", userId.getId());
		return ResponseEntity.ok(null);
	}


}
