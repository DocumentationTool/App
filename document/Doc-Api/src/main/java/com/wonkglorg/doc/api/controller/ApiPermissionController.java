package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_PERMISSION;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(API_PERMISSION)
public class ApiPermissionController{
	
	@GetMapping("user/get")
	public ResponseEntity<List<Permission<UserId>>> getUserPermissions(@RequestParam("repoId") String repoId,
																	   @RequestParam("userId") String userId) {
		return null;
	}
	
	@GetMapping("user/add")
	public ResponseEntity<RestResponse<Void>> addUserPermission(@RequestParam("repoId") String repoId,
																@RequestParam("userId") String userId,
																@RequestParam("permissionType") String permissionType,
																@RequestParam("path") String path) {
		return null;
	}
	
	@GetMapping("user/remove")
	public ResponseEntity<RestResponse<Void>> removeUserPermission(@RequestParam("repoId") String repoId,
																   @RequestParam("userId") String userId,
																   @RequestParam("permissionType") String permissionType,
																   @RequestParam("path") String path) {
		return null;
	}
	
	@GetMapping("group/get")
	public ResponseEntity<List<Permission<UserId>>> getGroupPermissions(@RequestParam("repoId") String repoId,
																		@RequestParam("groupId") String groupId) {
		return null;
	}
	
	@GetMapping("group/add")
	public ResponseEntity<RestResponse<Void>> addGroupPermission(@RequestParam("repoId") String repoId,
																 @RequestParam("groupId") String groupId,
																 @RequestParam("permissionType") String permissionType,
																 @RequestParam("path") String path) {
		return null;
	}
	
	@GetMapping("group/remove")
	public ResponseEntity<RestResponse<Void>> removeGroupPermission(@RequestParam("repoId") String repoId,
																	@RequestParam("groupId") String groupId,
																	@RequestParam("permissionType") String permissionType,
																	@RequestParam("path") String path) {
		return null;
	}
	
}
