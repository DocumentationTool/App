package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.wonkglorg.docapi.controller.Constants.ControllerPaths.API_DOCUMENT;

/**
 * Handles all api document specific requests
 */
@Controller
@RequestMapping(API_DOCUMENT)
public class ApiDocumentController {
	private static final Logger log = LoggerFactory.getLogger(ApiDocumentController.class);
	
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("{repo}/get/{docPath}")
	public ResponseEntity<UserProfile> addUser(@PathVariable("repo") String repo, @PathVariable("docPath") String docPath) {
		log.info("Received PUT request to get document from repo='{}' with docPath='{}'", repo, docPath);
		return ResponseEntity.ok(null);
	}
	
	
}
