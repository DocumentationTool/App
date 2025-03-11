package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.exception.InvalidTagException;
import com.wonkglorg.doc.api.exception.NotaRepoException;
import com.wonkglorg.doc.api.json.JsonFileTree;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.api.service.ResourceService;
import com.wonkglorg.doc.core.db.DbHelper;
import com.wonkglorg.doc.core.objects.*;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_RESOURCE;

/**
 * Handles all api document specific requests
 */
@RestController
@RequestMapping(API_RESOURCE)
public class ApiResourceController {
    private final ResourceService resourceService;
    private final RepoService repoService;

    public ApiResourceController(@Lazy RepoService repoService, ResourceService resourceService) {
        this.resourceService = resourceService;
        this.repoService = repoService;
    }

    @Operation(summary = "Gets a resource", description = """
            ## Returns resources by the specified request.
            
            ### searchTerm
            if null is given returns the result regardless of its file contents.
            ### path
            if null is given returns the result for all resources in the repository.
            ### repoId
            if null is given returns the result for all repositories.
            ### userId
            if null is given returns regardless of permissions, if a valid user is given limits the search to any file the user would have access to.
            ### whiteListTags
            requires a resource to at least have this tag to be returned.
            ### blacklistListTags
            requires a resource to not have this tag to be returned. If a resource has a whitelisted tag and a blacklisted tag it will not be returned as the blacklisted tag takes precedence.
            ### withData
            if true returns the data of the resource.
            ### returnLimit
            limits the amount of resources returned.
            """)
    @PostMapping("/get")
    public ResponseEntity<RestResponse<Map<String, List<JsonResource>>>> getResources(@RequestBody ResourceRequest request) {
        try {
            var resources = resourceService.getResources(request);
            if (resources.isError()) {
                return RestResponse.<Map<String, List<JsonResource>>>error(resources.getException().getMessage()).toResponse();
            }

            Map<String, List<JsonResource>> jsonResources = new HashMap<>();

            for (var resource : resources.get()) {
                jsonResources.computeIfAbsent(resource.repoId().id(), s -> new ArrayList<>()).add(JsonResource.of(resource));
            }
            return RestResponse.success(jsonResources).toResponse();

        } catch (Exception e) {
            return RestResponse.<Map<String, List<JsonResource>>>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Constructs a file tree", description = """
            ## Constructs a file tree out of the given resource request.
            
            ### searchTerm
            if null is given returns the result regardless of its file contents.
            ### path
            if null is given returns the result for all resources in the repository.
            ### repoId
            if null is given returns the result for all repositories.
            ### userId
            if null is given returns regardless of permissions, if a valid user is given limits the search to any file the user would have access to.
            ### whiteListTags
            requires a resource to at least have this tag to be returned.
            ### blacklistListTags
            requires a resource to not have this tag to be returned. If a resource has a whitelisted tag and a blacklisted tag it will not be returned as the blacklisted tag takes precedence.
            ### withData
            if true returns the data of the resource.
            ### returnLimit
            limits the amount of resources returned.
            """)
    @PostMapping("/get/filetree")
    public ResponseEntity<RestResponse<Map<String, JsonFileTree>>> getFiletree(@RequestBody ResourceRequest request) {
        try {
            var resources = resourceService.getResources(request);

            if (resources.isError()) {
                return RestResponse.<Map<String, JsonFileTree>>error(resources.getException().getMessage()).toResponse();
            }

            Map<String, JsonFileTree> fileTrees = new HashMap<>();

            for (var resource : resources.get()) {
                String repo = resource.repoId().id();
                String normalizedPath = resource.resourcePath().toString().replace("\\", "/");
                String[] pathSegments = normalizedPath.split("/");

                //build file tree
                JsonFileTree current = fileTrees.computeIfAbsent(repo, JsonFileTree::new);
                for (int i = 0; i < pathSegments.length; i++) {
                    String pathPart = pathSegments[i];

                    if (i == pathSegments.length - 1 && pathPart.contains(".")) {
                        // is a file
                        current.addResource(resource);
                    } else {
                        //its a directory
                        current = current.add(pathPart);
                    }
                }
            }

            return RestResponse.success(fileTrees).toResponse();

        } catch (Exception e) {
            return RestResponse.<Map<String, JsonFileTree>>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Adds a resource", description = """
            ## Body
            The data the file should contain.
            """)
    @PutMapping("/add")
    public ResponseEntity<RestResponse<Void>> insertResource(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                             @Parameter(description = "The path to the resource.") @RequestParam("path") String path,
                                                             @Parameter(description = "The user who created the resource.") @RequestParam("createdBy") String createdBy,
                                                             @Parameter(description = "The category of the resource.") @RequestParam(value = "category", required = false) String category,
                                                             @RequestParam(value = "tagIds", required = false) List<String> tagIds,
                                                             @RequestBody String content) {
        RepoId id = new RepoId(repoId);
        Path resourcePath = Path.of(path);

        if (!repoService.isValidRepo(id)) {
            return RestResponse.<Void>error("Repository '%s' does not exist".formatted(id.id())).toResponse();
        }

        if (tagIds == null) {
            tagIds = new ArrayList<>();
        }

        if (!path.endsWith(".md")) {
            return RestResponse.<Void>error("Resource must be a markdown file ending in .md").toResponse();
        }

        String allowedResponse = DbHelper.isAllowedPath(resourcePath);
        if (allowedResponse != null) {
            return RestResponse.<Void>error(allowedResponse).toResponse();
        }

        if (resourceService.resourceExists(id, resourcePath)) {
            return RestResponse.<Void>error("Resource already exists").toResponse();
        }

        List<Tag> tags = null;
        try {
            tags = resourceService.getTags(id, tagIds.stream().map(TagId::new).toList());
        } catch (InvalidTagException | NotaRepoException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }

        Resource resource = new Resource(resourcePath, createdBy, id, category, tags, content);
        return RestResponse.of(resourceService.insertResource(resource)).toResponse();
    }


    @Operation(summary = "Updates a resource", description = "Updates a resource in the Repository.")
    @PutMapping("/update")
    public ResponseEntity<RestResponse<Void>> updateResource(@RequestBody ResourceUpdateRequest request) {
        if (!repoService.isValidRepo(new RepoId(request.repoId))) {
            return RestResponse.<Void>error("Repository does not exist").toResponse();
        }
		/*
		if(DbHelper.isAllowedPath(Path.of(request.path))){
			return RestResponse.<Void>error("Invalid path form or blacklisted symbols used! Symbols include (% and ..)").toResponse();
		}
		
		 */

        if (!request.path.endsWith(".md")) {
            return RestResponse.<Void>error("Specified Resource must be a markdown file path ending in .md").toResponse();
        }

        if (!resourceService.resourceExists(new RepoId(request.repoId), Path.of(request.path))) {
            return RestResponse.<Void>error("Resource does not exist").toResponse();
        }

        QueryDatabaseResponse<Resource> response = resourceService.updateResource(request);
        if (response.isSuccess()) {
            return RestResponse.<Void>success(response.getResponseText(), null).toResponse();
        } else {
            return RestResponse.<Void>error(response.getException().getMessage()).toResponse();
        }
    }

    @Operation(summary = "Removes a resource", description = "Removes a resource from the Repository.")
    @PostMapping("/remove")
    public ResponseEntity<RestResponse<Void>> removeResource(@RequestParam("repo") String repo, @RequestParam("path") String path) {
        RepoId id = new RepoId(repo);
        if (!repoService.isValidRepo(id)) {
            return RestResponse.<Void>error("Repository does not exist").toResponse();
        }
		
		/*
		if(DbHelper.isAllowedPath(Path.of(path))){
			return RestResponse.<Void>error("Invalid path form or blacklisted symbols used! Symbols include (% and ..)").toResponse();
		}
		
		 */

        if (!path.endsWith(".md")) {
            return RestResponse.<Void>error("Resource path must point to a markdown file ending in .md").toResponse();
        }

        if (!resourceService.resourceExists(id, Path.of(path))) {
            return RestResponse.<Void>error("Resource does not exist").toResponse();
        }

        return RestResponse.of(resourceService.removeResource(id, Path.of(path))).toResponse();
    }

    @Operation(summary = "Moves a resource", description = "Moves a resource from one destination to another.")
    @PostMapping("/move")
    public ResponseEntity<RestResponse<Void>> moveResource(UserId userId, RepoId repoFrom, Path from, RepoId repoTo, Path to) {
        return RestResponse.of(resourceService.move(userId, repoFrom, from, repoTo, to)).toResponse();
    }

}
