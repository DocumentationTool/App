package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonGroup;
import com.wonkglorg.doc.api.json.JsonUser;
import com.wonkglorg.doc.api.service.GroupService;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_GROUP;

/**
 * Controller Endpoint handling all group related endpoints
 */
@RestController
@RequestMapping(API_GROUP)
public class ApiGroupController {

    private static final Logger log = LoggerFactory.getLogger(ApiGroupController.class);
    private final GroupService groupService;

    public ApiGroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Renames a given group
     *
     * @param repoId  The repository id to search in
     * @param groupId The groups id to be rename
     * @param newName the new name of the group
     * @return {@link RestResponse}
     */
    @Operation(summary = "Renames a Group", description = "Renames a given group")
    @PostMapping("rename")
    public ResponseEntity<RestResponse<Void>> renameGroup(@Parameter(description = "The repository id to search in.") @RequestParam("repoId") String repoId,
                                                          @Parameter(description = "The groups id to be rename.") @RequestParam("groupId") String groupId,
                                                          @Parameter(description = "The new name of the group.") @RequestParam("newName") String newName) {
        try {
            groupService.renameGroup(RepoId.of(repoId), GroupId.of(groupId), newName);
            return RestResponse.<Void>success("Updated group '%s' from repo '%s", null).toResponse();//.formatted(userId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while updating Group", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * Returns a group or groups if no groupId is given.
     *
     * @param repoId  The repoId to search in. If none is given, returns the result for all currently loaded repos.
     * @param groupId The groupid to search for, if none is given, returns all groups.
     * @return {@link RestResponse}
     */
    @Operation(summary = "Get groups", description = "Returns a group or groups if no groupId is given.")
    @GetMapping("get")
    public ResponseEntity<RestResponse<List<JsonGroup>>> getGroups(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
                                                                   @Parameter(description = "The groupid to search for, if none is given, returns all groups.") @RequestParam(value = "groupId", required = false) String groupId) {
        try {

            List<Group> users = groupService.getGroups(RepoId.of(repoId), GroupId.of(groupId));
            List<JsonGroup> jsonGroups = users.stream().map(JsonGroup::new).toList();
            return RestResponse.success(jsonGroups).toResponse();
        } catch (ClientException e) {
            return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while getting users ", e);
            return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
        }
    }

    /**
     * @param repoId
     * @param groupId
     * @return {@link RestResponse}
     */
    @Operation(summary = "Get Users from Group", description = "Returns all users in a group.")
    @GetMapping("get/all/users")
    public ResponseEntity<RestResponse<List<JsonUser>>> getAllGroupsForUser(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                                            @Parameter(description = "The groupId search for.") @RequestParam("groupId") String groupId) {
        try {
            List<UserProfile> users = groupService.getUsersFromGroup(RepoId.of(repoId), GroupId.of(groupId));
            List<JsonUser> jsonUsers = users.stream().map(JsonUser::new).toList();
            return RestResponse.success(jsonUsers).toResponse();
        } catch (ClientException e) {
            return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while getting users ", e);
            return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
        }
    }

    /**
     * @param repoId
     * @param userId
     * @return {@link RestResponse}
     */
    @Operation(summary = "Get groups from user", description = "Returns all groups a user is in.")
    @GetMapping("get/all/groups")
    public ResponseEntity<RestResponse<List<JsonGroup>>> getAllUsersForGroup(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                                             @Parameter(description = "The userId to return the groups for.") @RequestParam("userId") String userId) {
        try {
            List<Group> users = groupService.getGroupsFromUser(RepoId.of(repoId), UserId.of(userId));
            List<JsonGroup> jsonGroups = users.stream().map(JsonGroup::new).toList();
            return RestResponse.success(jsonGroups).toResponse();
        } catch (ClientException e) {
            return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while getting users ", e);
            return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
        }
    }

    /**
     * @param repoId
     * @param groupId
     * @return {@link RestResponse}
     */
    @Operation(summary = "Removes a Group", description = "Removes a group from the system.")
    @PostMapping("remove")
    public ResponseEntity<RestResponse<Void>> deleteGroup(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                          @Parameter(description = "The users id to remove.") @RequestParam("groupId") String groupId) {
        try {
            groupService.removeGroup(RepoId.of(repoId), GroupId.of(groupId));
            return RestResponse.<Void>success("Deleted group '%s' from repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * @param repoId
     * @param groupId
     * @param groupName
     * @return {@link RestResponse}
     */
    @Operation(summary = "Adds a new Group", description = "Adds a new Group to the repo.")
    @PostMapping("add")
    public ResponseEntity<RestResponse<Void>> addGroup(@Parameter(description = "The repoId to add the group to.") @RequestParam(value = "repoId") String repoId,
                                                       @Parameter(description = "The groupId.") @RequestParam("groupId") String groupId,
                                                       @Parameter(description = "The groupname.") @RequestParam("groupName") String groupName) {
        try {
            groupService.addGroup(RepoId.of(repoId), new Group(GroupId.of(groupId), groupName, "system", LocalDateTime.now()));
            return RestResponse.<Void>success("Added group '%s' to repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * @param repoId
     * @param userId
     * @param groupId
     * @return {@link RestResponse}
     */
    @Operation(summary = "Adds a user to a group", description = "Adds a user to a group in a repo.")
    @PostMapping("user/add")
    public ResponseEntity<RestResponse<Void>> addUserToGroup(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                             @Parameter(description = "The users id to add.") @RequestParam("userId") String userId,
                                                             @Parameter(description = "The group id to add the user to.") @RequestParam("groupId") String groupId) {
        try {
            groupService.addUserToGroup(RepoId.of(repoId), GroupId.of(groupId), UserId.of(userId));
            return RestResponse.<Void>success("Added user '%s' to group '%s' in repo '%s".formatted(userId, groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * @param repoId
     * @param userId
     * @param groupId
     * @return {@link RestResponse}
     */
    @Operation(summary = "Removes a user from a group", description = "Removes a user from a group in a repo.")
    @PostMapping("user/remove")
    public ResponseEntity<RestResponse<Void>> removeUserFromGroup(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                                  @Parameter(description = "The users id to remove.") @RequestParam("userId") String userId,
                                                                  @Parameter(description = "The group id to remove the user from.") @RequestParam("groupId") String groupId) {
        try {
            groupService.removeUserFromGroup(RepoId.of(repoId), GroupId.of(groupId), UserId.of(userId));
            return RestResponse.<Void>success("Removed user '%s' from group '%s' in repo '%s".formatted(userId, groupId, repoId), null).toResponse();

        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * Adds a permission to a group (this will fail if a permission with the same path already exists)
     * @param repoId the repository the group is in
     * @param groupId the groups id
     * @param type the type of permission to give
     * @param path the path of the permission
     * @return {@link RestResponse}
     */
    @Operation(summary = "Adds a permission to a group", description = "Adds a permission to a group in a repo.")
    @PostMapping("permission/add")
    public ResponseEntity<RestResponse<Void>> addGroupPermission(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                                 @Parameter(description = "The group id to add the permission to.") @RequestParam("groupId") String groupId,
                                                                 @Parameter(description = "The permission to add.") @RequestParam("permission") PermissionType type,
                                                                 @Parameter(description = "The path to add the permission to.") @RequestParam("path") String path) {
        try {
            Permission<GroupId> permission = new Permission<>(GroupId.of(groupId), type, TargetPath.of(path), RepoId.of(repoId));
            groupService.addPermissionToGroup(RepoId.of(repoId), permission);
            return RestResponse.<Void>success("Added permission to group '%s' in repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * Sets the permission of a group to a specific {@link PermissionType} this action will fail if no permission with the specified path exists yet
     *
     * @param repoId  The repository the group is in
     * @param groupId the groups id
     * @param type    the type of permission to set
     * @param path    the path of the permission
     * @return {@link RestResponse}
     */
    @Operation(summary = "Updates a permission of a group", description = "Updates a permission of a group in a repo.")
    @PostMapping("permission/update")
    public ResponseEntity<RestResponse<Void>> updateGroupPermission(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
                                                                    @Parameter(description = "The group id to update the permission for.") @RequestParam("groupId") String groupId,
                                                                    @Parameter(description = "The new permission to set.") @RequestParam("permission") PermissionType type,
                                                                    @Parameter(description = "The path to update the permission for.") @RequestParam("path") String path) {
        try {
            Permission<GroupId> permission = new Permission<>(GroupId.of(groupId), type, TargetPath.of(path), RepoId.of(repoId));
            groupService.updatePermissionForGroup(RepoId.of(repoId), permission);
            return RestResponse.<Void>success("Added permission to group '%s' in repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

    /**
     * Removes a permission from a group
     *
     * @param repoId  The repository id of the given group
     * @param groupId The group id to remove the permission from.
     * @param path    the path of the permission to remove
     * @return {@link RestResponse}
     */
    @Operation(summary = "Removes a permission from a group", description = "Removes a permission from a group in a repo.")
    @PostMapping("permission/remove")
    public ResponseEntity<RestResponse<Void>> removeGroupPermission(@Parameter(description = "The repository id of the given group.") @RequestParam("repoId") String repoId,
                                                                    @Parameter(description = "The group id to remove the permission from.") @RequestParam("groupId") String groupId,
                                                                    @Parameter(description = "The path of the permission to remove.") @RequestParam("path") String path) {
        try {
            groupService.removePermissionFromGroup(RepoId.of(repoId), GroupId.of(groupId), TargetPath.of(path));
            return RestResponse.<Void>success("Removed permission from group '%s' in repo '%s".formatted(groupId, repoId), null).toResponse();
        } catch (ClientException e) {
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<Void>error(e.getMessage()).toResponse();
        }
    }

}
