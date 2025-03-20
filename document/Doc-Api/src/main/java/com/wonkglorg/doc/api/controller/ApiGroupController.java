package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonGroup;
import com.wonkglorg.doc.api.service.UserService;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.user.Group;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_GROUP;

@RestController
@RequestMapping(API_GROUP)
public class ApiGroupController {

    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(ApiGroupController.class);

    public ApiGroupController(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Updates a Group", description = "Not implemented yet")
    @PostMapping("edit")
    public ResponseEntity<RestResponse<Void>> editGroup() {
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


    @Operation(summary = "Get groups", description = "Returns a group or groups if no groupId is given.")
    @GetMapping("get")
    public ResponseEntity<RestResponse<List<JsonGroup>>> getGroups(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
                                                                   @Parameter(description = "The groupid to search for, if none is given, returns all groups in the repository.") @RequestParam(value = "groupId", required = false) String groupId) {
        try {

            List<Group> users = userService.getGroups(RepoId.of(repoId), GroupId.of(groupId));
            List<JsonGroup> jsonGroups = users.stream().map(JsonGroup::new).toList();

            return RestResponse.success(jsonGroups).toResponse();
        } catch (ClientException e) {
            return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while getting users ", e);
            return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Removes a Group", description = "Removes a group from the system.")
    @PostMapping("remove")
    public ResponseEntity<RestResponse<Void>> deleteGroup(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                          @Parameter(description = "The users id to remove.") @RequestParam("groupId") String groupId) {
        try {
            userService.removeGroup(RepoId.of(repoId), GroupId.of(groupId));
            return RestResponse.<Void>success("Deleted group '%s' from repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    @Operation(summary = "Adds a new Group", description = "Adds a new Group to the repo.")
    @PostMapping("add")
    public ResponseEntity<RestResponse<Void>> addGroup(@Parameter(description = "The repoId to add the group to.") @RequestParam(value = "repoId") String repoId,
                                                       @Parameter(description = "The groupId.") @RequestParam("groupId") String groupId,
                                                       @Parameter(description = "The groupname.") @RequestParam("groupName") String groupName) {
        try {
            userService.addGroup(RepoId.of(repoId), new Group(GroupId.of(groupId), groupName, "system", LocalDateTime.now()));
            return RestResponse.<Void>success("Added group '%s' to repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }


    @PostMapping("user/add")
    public ResponseEntity<RestResponse<Void>> addUserToGroup() {
        try {

        }
    }

    @PostMapping("user/remove")
    public ResponseEntity<RestResponse<Void>> removeUserFromGroup() {
        try {

        }
    }


    @PostMapping("permission/add")
    public ResponseEntity<RestResponse<Void>> addGroupPermission() {
        try {

        }
    }

    @PostMapping("permission/remove")
    public ResponseEntity<RestResponse<Void>> removeGroupPermission() {
        try {

        }
    }

}
