package com.wonkglorg.docapi.controller;

import static com.wonkglorg.docapi.controller.Constants.ApiMappings.USER;
import static com.wonkglorg.docapi.controller.Constants.ApiMappings.USER_ADD;
import static com.wonkglorg.docapi.controller.Constants.ApiMappings.USER_REMOVE;
import static com.wonkglorg.docapi.controller.Constants.ApiMappings.USER_UPDATE;
import static com.wonkglorg.docapi.controller.Constants.ControllerPaths.API;
import com.wonkglorg.docapi.db.FileDB;
import com.wonkglorg.docapi.security.UserAuthenticationManager;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller()
@CrossOrigin(origins = "https://localhost:4200")
@RequestMapping(API)
//https://www.baeldung.com/spring-boot-angular-web 2.4
public class RequestController{
	
	private final FileDB fileDB;
	private final UserAuthenticationManager authManager;
	
	public RequestController(FileDB fileDB, UserAuthenticationManager authManager) {
		this.fileDB = fileDB;
		this.authManager = authManager;
	}
	
	//todo how to authenticate users
	
	@PutMapping(value = USER_ADD)
	public ResponseEntity<UserProfile> addUser(@RequestParam("userID") String userId, @RequestParam("passwordHash") String passwordHash) {
		return ResponseEntity.ok(null);
	}
	
	@PutMapping(value = USER_REMOVE)
	public ResponseEntity<UserProfile> deleteUser() {
		return ResponseEntity.ok(null);
	}
	
	@GetMapping(USER + "/{id}")
	public ResponseEntity<UserProfile> getUser(@PathVariable("id") String id, @RequestParam String password) {
		
		authManager.authenticate(id, password);
		
		return ResponseEntity.ok(null);
	}
	
	@PutMapping(USER_UPDATE)
	public ResponseEntity<UserProfile> updateUser(@RequestBody UserProfile userId) {
		return ResponseEntity.ok(null);
	}
	
}
