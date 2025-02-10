package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.db.FileDB;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;

@Controller
@CrossOrigin(origins = "https://localhost:4200")
//https://www.baeldung.com/spring-boot-angular-web 2.4
public class RequestController {

	private final FileDB fileDB;

	public RequestController(FileDB fileDB) {
		this.fileDB = fileDB;
	}

	//todo how to authenticate users

	@PutMapping(value = "/user/add")
	public ResponseEntity<UserProfile> addUser(@RequestParam("userID") String userId,
			@RequestParam("passwordHash") String passwordHash) {

	}

	@PutMapping(value = "/user/delete")
	public ResponseEntity<UserProfile> deleteUser() {

	}

	@GetMapping(value = "/document/{path}")
	public ResponseEntity<Document> getDocument(@PathVariable String path) {
		return new ResponseEntity<>(fileDB.getDocument(Path.of(path)), HttpStatus.OK);
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<UserProfile> getUser(@PathVariable("id") String id,
			@RequestParam String passwordHash) {

	}

	@GetMapping(value = "/user/{id}")
	public ResponseEntity<UserProfile> getUserProfile(@PathVariable String id) {
		return new ResponseEntity<>(fileDB.getUserProfile(id), HttpStatus.OK);
	}

	@PutMapping("/user/update")
	public ResponseEntity<UserProfile> updateUser(@RequestBody UserProfile userId) {

	}

	@PutMapping(value = "/user/update")
	public ResponseEntity<UserProfile> updateUser() {

	}


}
