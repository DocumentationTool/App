package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonFileTree;
import com.wonkglorg.doc.api.json.JsonResource;
import com.wonkglorg.doc.api.json.JsonResourceEdit;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.api.service.ResourceService;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.db.DbHelper;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.ResourceException;
import com.wonkglorg.doc.core.objects.*;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ApiResourceController.class);
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

        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Map<String, List<JsonResource>>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while retrieving resources", e);
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

        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Map<String, JsonFileTree>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while retrieving filetree", e);
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

        try {
            RepoId id = repoService.validateRepoId(repoId);
            Path resourcePath = Path.of(path);
            DbHelper.validatePath(resourcePath);
            DbHelper.validateFileType(resourcePath);
            if (resourceService.resourceExists(id, resourcePath)) {
                throw new IllegalArgumentException("The resource '%s' already exists in repository '%s'".formatted(resourcePath, id));
            }
            FileRepository repo = repoService.getRepo(id);
            repo.checkTags(tagIds);

            List<Tag> tags;
            if (tagIds != null && !tagIds.isEmpty()) {
                tags = resourceService.getTags(id, tagIds.stream().map(TagId::new).toList());
            } else {
                tags = new ArrayList<>();
            }
            Resource resource = new Resource(resourcePath, createdBy, id, category, tags, content);
            return RestResponse.of(resourceService.insertResource(resource)).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Failed while trying to insert Resource", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }


    @Operation(summary = "Updates a resource", description = "Updates a resource in the Repository.")
    @PutMapping("/update")
    public ResponseEntity<RestResponse<Void>> updateResource(@RequestBody ResourceUpdateRequest request) {
        try {
            RepoId id = repoService.validateRepoId(request.repoId);
            Path path = Path.of(request.path);
            DbHelper.validatePath(path);
            DbHelper.validateFileType(path);
            FileRepository repo = repoService.getRepo(id);
            if (!resourceService.resourceExists(id, path)) {
                throw new ResourceException(id, "Resource '%s' does not exist in repository '%s'".formatted(path, id));
            }
            repo.checkTags(request.tagsToSet);
            repo.checkTags(request.tagsToAdd);
            repo.checkTags(request.tagsToRemove);


            QueryDatabaseResponse<Resource> response = resourceService.updateResource(request);
            if (response.isSuccess()) {
                return RestResponse.<Void>success(response.getResponseText(), null).toResponse();
            } else {
                return RestResponse.<Void>error(response.getException().getMessage()).toResponse();
            }
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Failed to update resource", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Removes a resource", description = "Removes a resource from the Repository.")
    @PostMapping("/remove")
    public ResponseEntity<RestResponse<Void>> removeResource(@RequestParam("repoId") String repo, @RequestParam("path") String path) {
        try {
            RepoId id = repoService.validateRepoId(repo);
            Path pPath = Path.of(path);
            DbHelper.validatePath(pPath);
            DbHelper.validateFileType(pPath);

            if (!resourceService.resourceExists(id, pPath)) {
                throw new ResourceException(id, "Resource '%s' does not exist in repository '%s'".formatted(pPath, id));
            }

            if (resourceService.isBeingEdited(id, pPath)) {
                throw new CoreException(id, "Resource '%s' is currently being edited".formatted(pPath));
            }

            return RestResponse.of(resourceService.removeResource(id, pPath)).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while removing resources", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Moves a resource", description = "Moves a resource from one destination to another.")
    @PostMapping("/move")
    public ResponseEntity<RestResponse<Void>> moveResource(UserId userId, RepoId repoFrom, Path from, RepoId repoTo, Path to) {
        try {

        } catch (CoreException e) {

        } catch (Exception e) {

        }

        return RestResponse.of(resourceService.move(userId, repoFrom, from, repoTo, to)).toResponse();
    }

    @Operation(summary = "Adds a new Tag", description = "Adds a new Tag to the repository.")
    @PutMapping("tag/add")
    public ResponseEntity<RestResponse<Void>> addTag(
            @Parameter(description = "The repoId to add the tag in.")
            @RequestParam("repoId") String id,
            @Parameter(description = "the id of the tag to be added.")
            @RequestParam("tagId") String tagId,
            @Parameter(description = "the name to display for the tag id.")
            @RequestParam("tagName") String tagName) {
        try {
            RepoId repoId = repoService.validateRepoId(id);
            if (resourceService.tagExists(repoId, new TagId(tagId))) {
                throw new CoreException(repoId, "Tag '%s' already exists in repository '%s'".formatted(tagId, repoId));
            }

            Tag tag = new Tag(new TagId(tagId), tagName);
            return RestResponse.of(repoService.getRepo(repoId).getDatabase().addTag(tag)).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while adding resource", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Removes a Tag", description = "The tag to remove from the destination.")
    @PostMapping("/tag/remove")
    public ResponseEntity<RestResponse<Void>> removeTag(
            @Parameter(description = "The repoId to remove the tag from.") @RequestParam("repoId") String id,
            @Parameter(description = "The tagId to remove") @RequestParam("tagId") String tagId) {
        try {
            RepoId repoId = repoService.validateRepoId(id);
            if (resourceService.tagExists(repoId, new TagId(tagId))) {
                throw new CoreException(repoId, "Tag '%s' already exists in repository '%s'".formatted(tagId, repoId));
            }

            return RestResponse.of(repoService.getRepo(repoId).getDatabase().removeTag(new TagId(tagId))).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while adding resource", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Moves a resource", description = "The tag to retrieve")
    @PostMapping("/tag/get")
    public ResponseEntity<RestResponse<List<Tag>>> getTags(@Parameter(description = "The repoId to remove the tag from or null to remove the tag from all repositories.") @RequestParam("repoId") String id,
                                                           @Parameter(description = "The tagId to remove") @RequestParam("tagId") String tagId) {
        try {
            RepoId repoId = repoService.validateRepoId(id);
            if (resourceService.tagExists(repoId, new TagId(tagId))) {
                throw new CoreException(repoId, "Tag '%s' already exists in repository '%s'".formatted(tagId, repoId));
            }
            return RestResponse.of(repoService.getRepo(repoId).getDatabase().getTags(new TagId(tagId))).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<List<Tag>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while adding resource", e);
            return RestResponse.<List<Tag>>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Sets a resource as being edited", description = "Sets a resource as being edited. Needs to be released manually by the user.")
    @PutMapping("/editing/set")
    public ResponseEntity<RestResponse<Void>> setEditing(@RequestParam("repoId") String id, @RequestParam("path") String path, @RequestParam("userId") String userId) {
        try {
            RepoId repoId = repoService.validateRepoId(id);
            Path pPath = Path.of(path);
            if (!resourceService.resourceExists(repoId, pPath)) {
                throw new ResourceException(repoId, "Resource '%s' does not exist in repository '%s'".formatted(pPath, repoId));
            }
            UserId uId = repoService.validateUserId(repoId, userId);

            if (resourceService.isBeingEdited(repoId, pPath)) {
                throw new CoreException(repoId, "Resource '%s' is already being edited".formatted(pPath));
            }
            resourceService.setCurrentlyEdited(repoId, uId, pPath);
            return RestResponse.<Void>success("Set '%s' as being edited by '%s'".formatted(pPath, uId), null).toResponse();
        } catch (
                CoreException e) { //core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while setting edited ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Checks if a resource is being edited", description = "Returns information about a files editing status.")
    @GetMapping("/editing/get")
    public ResponseEntity<RestResponse<JsonResourceEdit>> isBeingEdited(@RequestParam("repoId") String id, @RequestParam("path") String path) {
        try {
            RepoId repoId = repoService.validateRepoId(id);
            Path pPath = Path.of(path);
            if (!resourceService.resourceExists(repoId, pPath)) {
                throw new ResourceException(repoId, "Resource '%s' does not exist in repository '%s'".formatted(pPath, repoId));
            }

            JsonResourceEdit response = new JsonResourceEdit();
            UserId editingUser = resourceService.getEditingUser(repoId, pPath);
            response.editingUser = editingUser != null ? editingUser.id() : null;
            response.isBeingEdited = editingUser != null;
            response.file = path;
            return RestResponse.success(response).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<JsonResourceEdit>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<JsonResourceEdit>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Removes a file as being edited", description = "Frees up a resource from its editing lock allowing anyone to take and edit it again.")
    @PostMapping("/editing/remove")
    public ResponseEntity<RestResponse<Void>> removeEditedState(@RequestParam("repoId") String id, @RequestParam("path") String path) {
        try {
            RepoId repoId = repoService.validateRepoId(id);
            Path pPath = Path.of(path);
            if (!resourceService.resourceExists(repoId, pPath)) {
                throw new ResourceException(repoId, "Resource '%s' does not exist in repository '%s'".formatted(pPath, repoId));
            }

            resourceService.removeCurrentlyEdited(repoId, pPath);
            return RestResponse.<Void>success("Removed '%s' as being edited".formatted(pPath), null).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }
}
