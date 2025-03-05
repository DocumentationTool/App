package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.exception.NotaRepoException;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_DOCUMENT;

/**
 * Handles all api document specific requests
 */
@Controller
@RequestMapping(API_DOCUMENT)
public class ApiDocumentController {
    private static final Logger log = LoggerFactory.getLogger(ApiDocumentController.class);
    private final RepoService repoService;

    public ApiDocumentController(RepoService repoService) {
        this.repoService = repoService;
    }

    @GetMapping("/get")
    public ResponseEntity<RestResponse<List<Resource>>> getResources(@RequestParam("repo") String repo) {
        log.info("Received PUT request to get resources from repo='{}'", repo);
        try {
            var resources = repoService.getRepo(new RepoId(repo)).getDatabase().getResources();
            return ResponseEntity.ok(RestResponse.of(resources));
        } catch (NotaRepoException e) {
            return ResponseEntity.badRequest().body(RestResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/add")
    public ResponseEntity<RestResponse<Void>> addResource(@RequestParam("repo") String repo, @RequestParam("path") String path, @RequestParam("createdBy") String createdBy, @RequestParam("category") String category, @RequestBody String content) {
        log.info("Received PUT request to add resource to repo='{}'", repo);
        try {
            repoService.getRepo(new RepoId(repo)).getDatabase().insertResource(path, createdBy, category, content);
            return ResponseEntity.ok(RestResponse.of());
        } catch (NotaRepoException e) {
            return ResponseEntity.badRequest().body(RestResponse.error(e.getMessage()));
        }
    }
}
