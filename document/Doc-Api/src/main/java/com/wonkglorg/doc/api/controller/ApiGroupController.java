package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_GROUP;
import com.wonkglorg.doc.api.json.JsonGroup;
import com.wonkglorg.doc.api.json.JsonUser;
import com.wonkglorg.doc.api.service.PermissionService;
import com.wonkglorg.doc.api.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Controller Endpoint handling all group related endpoints
 */
@RestController
@RequestMapping(API_GROUP)
public class ApiGroupController{
	
	private static final Logger log = LoggerFactory.getLogger(ApiGroupController.class);
	private final UserService userService;
	private final PermissionService permissionService;
	
	public ApiGroupController(UserService userService, PermissionService permissionService) {
		this.userService = userService;
		this.permissionService = permissionService;
	}
	
	/**
	 * Renames a given group
	 *
	 * @param groupId The groups id to be rename
	 * @param newName the new name of the group
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Renames a Group", description = "Renames a given group")
	@PostMapping("rename")
	public ResponseEntity<RestResponse<Void>> renameGroup(@Parameter(description = "The groups id to be rename.") @RequestParam("groupId") String groupId,
														  @Parameter(description = "The new name of the group.") @RequestParam("newName") String newName) {
		try{
			userService.renameGroup(GroupId.of(groupId), newName);
			return RestResponse.<Void>success("Updated group '%s' from repo '%s", null).toResponse();//.formatted(userId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while updating Group", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Returns a group or groups if no groupId is given.
	 *
	 * @param groupId The groupid to search for, if none is given, returns all groups.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Get groups", description = "Returns a group or groups if no groupId is given.")
	@GetMapping("get")
	public ResponseEntity<RestResponse<List<JsonGroup>>> getGroups(@Parameter(description = "The groupid to search for, if none is given, returns all groups.") @RequestParam(value = "groupId", required = false) String groupId) {
		try{
			
			List<JsonGroup> jsonGroups;
			if(groupId == null){
				List<Group> groups = userService.getGroups();
				jsonGroups = groups.stream().map(JsonGroup::new).toList();
			} else {
				Group group = userService.getGroup(GroupId.of(groupId));
				jsonGroups = List.of(new JsonGroup(group));
			}
			return RestResponse.success(jsonGroups).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while getting users ", e);
			return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Returns all users in a group.
	 *
	 * @param groupId the groupId search for
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Get Users from Group", description = "Returns all users in a group.")
	@GetMapping("get/all/users")
	public ResponseEntity<RestResponse<List<JsonUser>>> getAllGroupsForUser(@Parameter(description = "The groupId search for.") @RequestParam("groupId") String groupId) {
		try{
			Set<UserProfile> users = userService.getUsersFromGroup(GroupId.of(groupId));
			List<JsonUser> jsonUsers = users.stream().map(JsonUser::new).toList();
			return RestResponse.success(jsonUsers).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while getting users ", e);
			return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Returns all groups a user is in.
	 *
	 * @param userId the userId to return the groups for
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Get groups from user", description = "Returns all groups a user is in.")
	@GetMapping("get/all/groups")
	public ResponseEntity<RestResponse<List<JsonGroup>>> getAllUsersForGroup(@Parameter(description = "The userId to return the groups for.") @RequestParam("userId") String userId) {
		try{
			Set<Group> users = userService.getGroupsFromUser(UserId.of(userId));
			List<JsonGroup> jsonGroups = users.stream().map(JsonGroup::new).toList();
			return RestResponse.success(jsonGroups).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while getting users ", e);
			return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Removes a group from the system
	 *
	 * @param groupId the group id to remove
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Removes a Group", description = "Removes a group from the system.")
	@PostMapping("remove")
	public ResponseEntity<RestResponse<Void>> deleteGroup(@Parameter(description = "The users id to remove.") @RequestParam("groupId") String groupId) {
		try{
			userService.removeGroup(GroupId.of(groupId));
			return RestResponse.<Void>success("Deleted group '%s'".formatted(groupId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Adds a new group to the repository
	 *
	 * @param groupId the group id
	 * @param groupName the group name
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Adds a new Group", description = "Adds a new Group to the repo.")
	@PostMapping("add")
	public ResponseEntity<RestResponse<Void>> addGroup(@Parameter(description = "The groupId.") @RequestParam("groupId") String groupId,
													   @Parameter(description = "The groupname.") @RequestParam("groupName") String groupName) {
		try{
			userService.addGroup(new Group(GroupId.of(groupId), groupName, "system", LocalDateTime.now()));
			return RestResponse.<Void>success("Added group '%s'".formatted(groupId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Adds a user to a group
	 *
	 * @param userId the users id to add
	 * @param groupId the group id to add the user to
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Adds a user to a group", description = "Adds a user to a group in a repo.")
	@PostMapping("user/add")
	public ResponseEntity<RestResponse<Void>> addUserToGroup(@Parameter(description = "The users id to add.") @RequestParam("userId") String userId,
															 @Parameter(description = "The group id to add the user to.") @RequestParam("groupId") String groupId) {
		try{
			userService.addUserToGroup(GroupId.of(groupId), UserId.of(userId));
			return RestResponse.<Void>success("Added user '%s' to group '%s'".formatted(userId, groupId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Removes a user from a group
	 *
	 * @param userId the users id to remove
	 * @param groupId the group id to remove the user from
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Removes a user from a group", description = "Removes a user from a group in a repo.")
	@PostMapping("user/remove")
	public ResponseEntity<RestResponse<Void>> removeUserFromGroup(@Parameter(description = "The users id to remove.") @RequestParam("userId") String userId,
																  @Parameter(description = "The group id to remove the user from.") @RequestParam("groupId") String groupId) {
		try{
			userService.removeUserFromGroup(GroupId.of(groupId), UserId.of(userId));
			return RestResponse.<Void>success("Removed user '%s' from group '%s'".formatted(userId, groupId), null).toResponse();
			
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Adds a permission to a group (this will fail if a permission with the same path already exists)
	 *
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
		try{
			Permission<GroupId> permission = new Permission<>(GroupId.of(groupId), type, TargetPath.of(path), RepoId.of(repoId));
			permissionService.addPermissionToGroup(RepoId.of(repoId), permission);
			return RestResponse.<Void>success("Added permission to group '%s' in repo '%s".formatted(groupId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Sets the permission of a group to a specific {@link PermissionType} this action will fail if no permission with the specified path exists yet
	 *
	 * @param repoId The repository the group is in
	 * @param groupId the groups id
	 * @param type the type of permission to set
	 * @param path the path of the permission
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Updates a permission of a group", description = "Updates a permission of a group in a repo.")
	@PostMapping("permission/update")
	public ResponseEntity<RestResponse<Void>> updateGroupPermission(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
																	@Parameter(description = "The group id to update the permission for.") @RequestParam("groupId") String groupId,
																	@Parameter(description = "The new permission to set.") @RequestParam("permission") PermissionType type,
																	@Parameter(description = "The path to update the permission for.") @RequestParam("path") String path) {
		try{
			Permission<GroupId> permission = new Permission<>(GroupId.of(groupId), type, TargetPath.of(path), RepoId.of(repoId));
			permissionService.updatePermissionForGroup(RepoId.of(repoId), permission);
			return RestResponse.<Void>success("Added permission to group '%s' in repo '%s".formatted(groupId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Removes a permission from a group
	 *
	 * @param repoId The repository id of the given group
	 * @param groupId The group id to remove the permission from.
	 * @param path the path of the permission to remove
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Removes a permission from a group", description = "Removes a permission from a group in a repo.")
	@PostMapping("permission/remove")
	public ResponseEntity<RestResponse<Void>> removeGroupPermission(@Parameter(description = "The repository id of the given group.") @RequestParam("repoId") String repoId,
																	@Parameter(description = "The group id to remove the permission from.") @RequestParam("groupId") String groupId,
																	@Parameter(description = "The path of the permission to remove.") @RequestParam("path") String path) {
		try{
			permissionService.removePermissionFromGroup(RepoId.of(repoId), GroupId.of(groupId), TargetPath.of(path));
			return RestResponse.<Void>success("Removed permission from group '%s' in repo '%s".formatted(groupId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
}
