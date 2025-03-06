package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_DOCUMENT;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.service.RepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//todo:jmd give descriptions to the rest endpoints

/**
 * Handles all api document specific requests
 */
@RestController
@RequestMapping(API_DOCUMENT)
public class ApiDocumentController{
	private static final Logger log = LoggerFactory.getLogger(ApiDocumentController.class);
	private final RepoService repoService;
	
	public ApiDocumentController(RepoService repoService) {
		this.repoService = repoService;
	}
	
	@GetMapping("/get")
	public ResponseEntity<RestResponse<List<JsonResource>>> getResources(@RequestParam(value = "repo", required = false) String repoId,
																		 @RequestParam(value = "user", required = false) String userId) {
		log.info("Received PUT request to get resources");
        /*
        List<Resource> resources = null;

        try {
            if (repoId.id() != null) {
                repoService.getRepo(repoId);
            }
        } catch (Exception e) {
            return RestResponse.<List<JsonResource>>error(e.getMessage()).toResponse();
        }

        if (DEV_MODE) {
            resources = DEV_RESOURCES.stream().toList();
        } else {
            resources = repoService.getResources(repoId, userId).get();
        }
        resources = resources.stream().filter(r -> r.repoId().filter().test(repoId)).collect(Collectors.toList());
        return RestResponse.success(JsonResource.of(resources)).toResponse();
        
         */
		return null;
	}
	
	@GetMapping("/get/filetree")
	public ResponseEntity<RestResponse<List<JsonResource>>> getFiletree(@RequestParam("repo") String repo, @RequestParam("user") String user) {
		
		//todo:jmd optional depending on what parameters are given, if its non get everything everything, if its repo only do only repo without perms, if user is involved only give stuff based on user perms
		
		return null;
	}
	
	@PutMapping("/add")
	public ResponseEntity<RestResponse<Void>> addResource(@RequestParam("repo") String repo,
														  @RequestParam("path") String path,
														  @RequestParam("createdBy") String createdBy,
														  @RequestParam("category") String category,
														  @RequestBody String content) {
		log.info("Received PUT request to add resource to repo='{}'", repo);
        /*
        try {
            //repoService.getRepo(new RepoId(repo)).getDatabase().insertResource(path, createdBy, category, content);
           // return ResponseEntity.ok(RestResponse.of());
            return null;
        } catch (NotaRepoException e) {
            return ResponseEntity.badRequest().body(RestResponse.error(e.getMessage()));
        }

         */
		return null;
	}
	
	@PutMapping("/update")
	public ResponseEntity<RestResponse<Void>> updateResource() {
		return null;
	}
}
