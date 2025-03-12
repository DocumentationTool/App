package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonUser;
import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.api.service.UserService;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;

/**
 * Handles all api user specific requests
 */
@RestController
@RequestMapping(API_USER)
public class ApiUserController {
    /**
     * Logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);
    private final RepoService repoService;
    private final UserService userService;

    public ApiUserController(@Lazy RepoService repoService, @Lazy UserService userService) {
        this.repoService = repoService;
        this.userService = userService;
    }

    @Operation(
            summary = "Get a user",
            description = "Returns a user or users if no repository is given. If a repository is given, only returns users for that repository will be returned, if no userId is given returns all users in this repository."
    )
    @GetMapping("/get")
    public ResponseEntity<RestResponse<List<JsonUser>>> getUsers(
            @Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
            @RequestParam(value = "repoId") String repoId,
            @Parameter(description = "The userId to search for, if none is given, returns all users in the repository.")
            @RequestParam(value = "userId", required = false) String userId) {
        try {
            RepoId repo = repoService.validateRepoId(repoId);
            UserId user = repoService.validateUserId(repo, userId);
            return RestResponse.success("", userService.getUsers(repo,user));
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Map<String, JsonUser>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Map<String, JsonUser>>error(e.getMessage()).toResponse();
        }
    }

    @Operation(
            summary = "Adds a new user",
            description = "Adds a new user to the system. If a repository is given, only adds the user to that repository. If none is given, adds the user to all repositories."
    )
    @PutMapping("/add")
    public ResponseEntity<RestResponse<Void>> addUser(
            @Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
            @RequestParam(value = "repoId") String repoId,
            @Parameter(description = "The user's id.")
            @RequestParam("userId") String userId,
            @Parameter(description = "The user's password.")
            @RequestParam("password") String password) {
        try {
            RepoId repo = repoService.validateRepoId(repoId);
            UserId user = new UserId(userId);
            if (userService.userExists(repo, user)) {
                throw new CoreException(repo, "User with id '%s' already exists".formatted(userId));
            }
            userService.createUser(repo, user, password);
            return RestResponse.<Void>success("Added user '%s' to repo '%s".formatted(userId, repoId), null).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(
            summary = "Removes a User",
            description = "Removes a user from the system."
    )
    @PutMapping("/remove")
    public ResponseEntity<RestResponse<Void>> deleteUser(
            @Parameter(description = "The repoId to search in.")
            @RequestParam("repoId") String repoId,
            @Parameter(description = "The users id to remove.")
            @RequestParam("userId") String userId) {
        try {
            RepoId repo = repoService.validateRepoId(repoId);
            UserId user = repoService.validateUserId(repo, userId);
            userService.deleteUser(repo, user);
            return RestResponse.<Void>success("Deleted user '%s' from repo '%s", null).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }
}
