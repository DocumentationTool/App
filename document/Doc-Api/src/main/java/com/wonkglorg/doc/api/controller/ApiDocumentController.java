package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_DOCUMENT;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.util.List;

/**
 * Handles all api document specific requests
 */
@Controller
@RequestMapping(API_DOCUMENT)
public class ApiDocumentController{
	private static final Logger log = LoggerFactory.getLogger(ApiDocumentController.class);
	private final RepoService repoService;
	
	public ApiDocumentController(RepoService repoService) {
		this.repoService = repoService;
	}
	
	@GetMapping("/get")
	public ResponseEntity<RestResponse<List<Resource>>> getResources(@RequestParam("repo") String repo) {
		log.info("Received PUT request to get resources from repo='{}'", repo);
		
		RepoId repoId = new RepoId(repo);
		
		QueryDatabaseResponse<List<Resource>> resources = repoService.getResources(repoId);
		return ResponseEntity.ok(RestResponse.of(resources));
	}
	
	@PutMapping("/add")
	public ResponseEntity<RestResponse<Void>> addResource(@RequestParam("repo") String repo, @RequestParam("path") String path, @RequestParam("createdBy") String createdBy, @RequestParam("category") String category, @RequestBody String content) {
		log.info("Received PUT request to add resource to repo='{}'", repo);
		
		RepoId repoId = new RepoId(repo);

		//todo:jmd add repo calls here
		
		return ResponseEntity.ok(RestResponse.of(null));
	}
	
	
}
