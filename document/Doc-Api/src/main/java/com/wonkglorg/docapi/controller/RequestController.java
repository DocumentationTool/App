package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.db.FileDB;
import com.wonkglorg.docapi.properties.PortProperties;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;

@Controller
@CrossOrigin(origins = "https://localhost:4200") //https://www.baeldung.com/spring-boot-angular-web 2.4
public class RequestController {

	private final FileDB fileDB;
	private final PortProperties portProperties;

	public RequestController(FileDB fileDB, PortProperties portProperties) {
		this.fileDB = fileDB;
		this.portProperties = portProperties;
	}

	@GetMapping(value = "/document/{path}")
	public ResponseEntity<Document> getDocument(@PathVariable String path) {
		return new ResponseEntity<>(fileDB.getDocument(Path.of(path)), HttpStatus.OK);
	}

	@GetMapping(value = "/user/{id}")
	public ResponseEntity<UserProfile> getUserProfile(@PathVariable String id) {
		return new ResponseEntity<>(fileDB.getUserProfile(id), HttpStatus.OK);
	}
}
