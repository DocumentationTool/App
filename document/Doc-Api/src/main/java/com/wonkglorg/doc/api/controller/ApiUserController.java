package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
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
	
	/*
	@GetMapping("/get")
	public ResponseEntity<UserProfile> getUser(@RequestParam("repoId") String repoId, @RequestParam("userId") String userId) {
		RepoId id = if(repoService.validateRepoId(repoId)){
			return RestResponse.<List<Role>>error("Invalid Repo").toResponse();
		}
		if(DEV_MODE){
			Map<UserId, UserProfile> userProfileMap = DEV_USERS.get(id);
			if(userProfileMap == null){
				return RestResponse.<List<Role>>error("No users in repo found").toResponse();
			}
			UserProfile userProfile = userProfileMap.get(new UserId(userId));
			return RestResponse.success(userProfile.getRoles().stream().toList()).toResponse();
		}
		//return repoService.getUserPermissions(repoId,userId);
		return null;
	}
	
	 */
	
	@GetMapping("/get")
	public ResponseEntity<List<Role>> getUser(@RequestParam("repoId") String repoId, @RequestParam("userId") String userId) {
		return null;
	}
	
	@PutMapping("/add")
	public ResponseEntity<String> addUser(@RequestParam("repo") String repoId,
										  @RequestParam("userID") String userId,
										  @RequestParam("password") String password) {
		return null;
	}
	
	@PutMapping("/remove")
	public ResponseEntity<Profile> deleteUser(@RequestParam("userID") String userId) {
		log.info("Received PUT request to delete user with userID='{}'", userId);
		return ResponseEntity.ok(null);
	}
	
	@PutMapping("/update")
	public ResponseEntity<Profile> updateUser(@RequestBody UserProfile userId) {
		return null;
	}
	
}
