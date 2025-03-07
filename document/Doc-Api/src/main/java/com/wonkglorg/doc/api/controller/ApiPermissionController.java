package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonPermissions;
import com.wonkglorg.doc.api.service.RepoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_PERMISSION;

@RestController
@RequestMapping(API_PERMISSION)
public class ApiPermissionController {

    private final RepoService repoService;

    public ApiPermissionController(RepoService repoService) {
        this.repoService = repoService;
    }

    //@formatter:off
    /* can be added if specific info for swagger should be shown what to expect
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved Data"),
            @ApiResponse(responseCode = "404", description = "Error occurred during execution")
    })
     */
    @Operation(
            summary = "Get a user's Permissions",
            description = "Returns a list of permissions the user has. If a repository is given, only returns permissions for that repository. If none is given, returns permissions for all repositories."
    )
    @GetMapping("user/get")
    public ResponseEntity<RestResponse<Map<String,JsonPermissions>>> getUserPermissions(
            @Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
            @RequestParam(value = "repoId", required = false) String repoId,
            @Parameter(description = "The userId to retrieve the permissions for. If none or an invalid one is given, returns an error code.")
            @RequestParam("userId") String userId) {
        return null;
    }

    @Operation(
            summary = "Add a new user permission",
            description = "Adds a new user permission to a repository. If none is specified, adds the user permission to all repositories."
    )
    @GetMapping("user/add")
    public ResponseEntity<RestResponse<Void>> addUserPermission(
            @Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
            @RequestParam(value = "repoId", required = false) String repoId,
            @Parameter(description = "The userId to assign the permission to.")
            @RequestParam("userId") String userId,
            @Parameter(description = "The type of permission to assign to the user.")
            @RequestParam("permissionType") String permissionType,
            @Parameter(description = "The path or scope the permission applies to.")
            @RequestParam("path") String path) {
        return null;
    }

    @Operation(
            summary = "Remove a user's permission",
            description = "Removes a specific user's permission from a repository. If none is specified, removes the permission from all repositories."
    )
    @GetMapping("user/remove")
    public ResponseEntity<RestResponse<Void>> removeUserPermission(
            @Parameter(description = "The repoId to remove the user's permission from.")
            @RequestParam("repoId") String repoId,
            @Parameter(description = "The userId whose permission is to be removed.")
            @RequestParam("userId") String userId,
            @Parameter(description = "The type of permission to remove from the user.")
            @RequestParam("permissionType") String permissionType,
            @Parameter(description = "The path or scope where the permission will be removed.")
            @RequestParam("path") String path) {
        return null;
    }

    @Operation(
            summary = "Get group permissions",
            description = "Returns the list of permissions a specific group has in a given repository."
    )
    @GetMapping("group/get")
    public ResponseEntity<RestResponse<Map<String,JsonPermissions>>> getGroupPermissions(
            @Parameter(description = "The repoId to search the group's permissions in.")
            @RequestParam("repoId") String repoId,
            @Parameter(description = "The groupId to retrieve the permissions for.")
            @RequestParam("groupId") String groupId) {
        return null;
    }

    @Operation(
            summary = "Add group permission",
            description = "Adds a new group permission to a repository. If none is specified, adds the group permission to all repositories."
    )
    @GetMapping("group/add")
    public ResponseEntity<RestResponse<Void>> addGroupPermission(
            @Parameter(description = "The repoId to add the group's permission to.")
            @RequestParam("repoId") String repoId,
            @Parameter(description = "The groupId to assign the permission to.")
            @RequestParam("groupId") String groupId,
            @Parameter(description = "The type of permission to assign to the group.")
            @RequestParam("permissionType") String permissionType,
            @Parameter(description = "The path or scope the permission applies to.")
            @RequestParam("path") String path) {
        return null;
    }

    @Operation(
            summary = "Remove group permission",
            description = "Removes a specific group's permission from a repository. If none is specified, removes the permission from all repositories."
    )
    @GetMapping("group/remove")
    public ResponseEntity<RestResponse<Void>> removeGroupPermission(
            @Parameter(description = "The repoId to remove the group's permission from.")
            @RequestParam("repoId") String repoId,
            @Parameter(description = "The groupId whose permission is to be removed.")
            @RequestParam("groupId") String groupId,
            @Parameter(description = "The type of permission to remove from the group.")
            @RequestParam("permissionType") String permissionType,
            @Parameter(description = "The path or scope where the permission will be removed.")
            @RequestParam("path") String path) {
        return null;
    }
	//@formatter:on

}
