package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_PERMISSION;
import com.wonkglorg.doc.core.objects.Identifyable;
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
	
	//todo:jmd add
	@GetMapping("/get")
	public ResponseEntity<List<Permission<Identifyable>>> getPermissions(@RequestParam("repoId") String repoId,
																		 @RequestParam("userId") String userId) {
		return null;
	}
	
	@GetMapping("/add")
	public ResponseEntity<RestResponse<Void>> addPermission(@RequestParam("repoId") String repoId,
															@RequestParam("userId") String userId,
															@RequestParam String permission,
															@RequestParam String path) {
		return null;
	}
	
	@GetMapping("/remove")
	public ResponseEntity<RestResponse<Void>> removePermission(@RequestParam String userId,
															   @RequestParam String permission,
															   @RequestParam String path) {
		return null;
	}
	
}
