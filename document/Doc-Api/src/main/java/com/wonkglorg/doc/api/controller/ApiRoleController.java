package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;
import static com.wonkglorg.doc.api.DocApiApplication.DEV_USERS;
import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_ROLE;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.RoleId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(API_ROLE)
public class ApiRoleController{
	
	private final RepoService repoService;
	
	public ApiRoleController(RepoService repoService) {
		this.repoService = repoService;
	}
	
	@GetMapping("/get")
	public ResponseEntity<RestResponse<List<Role>>> getRoles(@RequestParam("repoId") String repoId, @RequestParam("userId") String userId) {
		RepoId rId = new RepoId(repoId);
		UserId uId = new UserId(userId);
		
		if(DEV_MODE){
			Map<UserId, UserProfile> userProfileMap = DEV_USERS.get(rId);
			if(userProfileMap == null){
				return RestResponse.<List<Role>>error("No users in repo found").toResponse();
			}
			UserProfile userProfile = userProfileMap.get(uId);
			return RestResponse.success(userProfile.getRoles().stream().toList()).toResponse();
		}
		
		return RestResponse.of(repoService.getRoles(rId, uId)).toResponse();
	}
	
	@GetMapping("/get/all")
	public ResponseEntity<RestResponse<List<Role>>> getAllRoles(@RequestParam("repoId") String repoId) {
		RepoId id = new RepoId(repoId);
		if(repoService.isValidRepo(id)){
			return RestResponse.<List<Role>>error("Invalid Repo").toResponse();
		}
		if(DEV_MODE){
			Map<UserId, UserProfile> userProfileMap = DEV_USERS.get(id);
			if(userProfileMap == null){
				return RestResponse.<List<Role>>error("No users in repo found").toResponse();
			}
			List<Role> roles = userProfileMap.values().stream().map(UserProfile::getRoles).flatMap(Set::stream).toList();
			return RestResponse.success(roles).toResponse();
		}
		//return repoService.getAllRoles(repoId);
		return null;
	}
	
	@PostMapping("/add")
	public ResponseEntity<RestResponse<Void>> addRole(@RequestParam("repoId") String repoId,
													  @RequestParam("userId") String userId,
													  @RequestParam("roleId") String roleId) {
		RepoId id = new RepoId(repoId);
		if(repoService.isValidRepo(id)){
			return RestResponse.<Void>error("Invalid Repo").toResponse();
		}
		
		if(DEV_MODE){
			UserProfile userProfile = DEV_USERS.putIfAbsent(id, new HashMap<>()).get(new UserId(userId));
			if(userProfile == null){
				return RestResponse.<Void>error("User doesn't exist").toResponse();
			}
			userProfile.getRoles().add(new Role(new RoleId(roleId), "Role"));
			return RestResponse.<Void>success("Added Role to user'", null).toResponse();
		}
		
		//return RestResponse.success("Role added").toResponse();
		return null;
	}
	
	@PostMapping("/remove")
	public ResponseEntity<RestResponse<Void>> removeRole(@RequestParam("repoId") String repoId,
														 @RequestParam("userId") String userId,
														 @RequestParam("roleId") String role) {
		RepoId id = new RepoId(repoId);
		//return RestResponse.success("Role removed").toResponse();
		if(repoService.isValidRepo(id)){
			return RestResponse.<Void>error("Invalid Repo").toResponse();
		}
		
		if(DEV_MODE){
			//DEV_USERS.get(userId).remove(role);
			return RestResponse.<Void>success("Role removed", null).toResponse();
		}
		
		//return RestResponse.success("Role removed").toResponse();
		return null;
	}
	
}
