package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonUser;
import com.wonkglorg.doc.api.service.UserService;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;

/**
 * Handles all api user specific requests
 */
//todo:jmd all calls are working!
@RestController
@RequestMapping(API_USER)
public class ApiUserController {
    /**
     * Logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);
    private final UserService userService;

    public ApiUserController(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get a user", description = "Returns a user or users if no repository is given. If a repository is given, only returns users for that repository will be returned, if no userId is given returns all users in this repository.")
    @GetMapping("/get")
    public ResponseEntity<RestResponse<List<JsonUser>>> getUsers(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
                                                                 @Parameter(description = "The userId to search for, if none is given, returns all users in the repository.") @RequestParam(value = "userId", required = false) String userId) {
        try {
            List<UserProfile> users = userService.getUsers(repoId, userId);
            List<JsonUser> jsonUsers = users.stream().map(JsonUser::new).toList();

            return RestResponse.success("", jsonUsers).toResponse();
        } catch (ClientException e) {
            return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while getting users ", e);
            return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
        }
    }


    @Operation(summary = "Adds a new user", description = "Adds a new user to the system. If a repository is given, only adds the user to that repository. If none is given, adds the user to all repositories.")
    @PostMapping("/add")
    public ResponseEntity<RestResponse<Void>> addUser(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
                                                      @Parameter(description = "The user's id.") @RequestParam("userId") String userId,
                                                      @Parameter(description = "The user's password.") @RequestParam("password") String password,
                                                      @Parameter(description = "The user's group.") @RequestParam(value = "groupIds", required = false) Set<String> groupIds) {
        try {
            if (groupIds == null) {
                groupIds = Set.of();
            }
            Set<GroupId> groupIdSet = groupIds.stream().map(GroupId::of).collect(Collectors.toSet());

            userService.addUser(RepoId.of(repoId), new UserProfile(UserId.of(userId), password, Set.of(), Set.of(), groupIdSet));
            return RestResponse.<Void>success("Added user '%s' to repo '%s".formatted(userId, repoId), null).toResponse();
        } catch (
                CoreException e) {//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Updates a User", description = "Not implemented yet")
    @PostMapping("/edit")
    public ResponseEntity<RestResponse<Void>> editUser() {
        try {
            //todo:jmd implement
            //userService.updateUser(RepoId.of(null), UserId.of(null));
            throw new ClientException("Not implemented yet");
            //return RestResponse.<Void>success("Updated user '%s' from repo '%s", null).toResponse();//.formatted(userId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }


    @Operation(summary = "Removes a User", description = "Removes a user from the system.")
    @PostMapping("/remove")
    public ResponseEntity<RestResponse<Void>> deleteUser(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                         @Parameter(description = "The users id to remove.") @RequestParam("userId") String userId) {
        try {
            userService.removeUser(RepoId.of(repoId), UserId.of(userId));
            return RestResponse.<Void>success("Deleted user '%s' from repo '%s".formatted(userId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }


    @PostMapping("group/add")
    public ResponseEntity<RestResponse<Void>> addUserToGroup() {
        try {

        }
    }

    @PostMapping("group/remove")
    public ResponseEntity<RestResponse<Void>> groupToAddUserTo() {
        try {

        }
    }


    @PostMapping("permission/add")
    public ResponseEntity<RestResponse<Void>> addUserPermission() {
        try {

        }
    }

    @PostMapping("permission/remove")
    public ResponseEntity<RestResponse<Void>> removeUserPermission() {
        try {

        }
    }
}
