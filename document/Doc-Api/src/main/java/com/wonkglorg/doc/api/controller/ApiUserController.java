package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;
import com.wonkglorg.doc.api.json.JsonPermission;
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
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles all api user specific requests
 */
//todo:jmd all calls are working!
@RestController
@RequestMapping(API_USER)
public class ApiUserController{
	/**
	 * Logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);
	private final UserService userService;
	private final PermissionService permissionService;
	
	public ApiUserController(@Lazy UserService userService, PermissionService permissionService) {
		this.userService = userService;
		this.permissionService = permissionService;
	}
	
	/**
	 * Get users
	 *
	 * @param userId The userId to search for, if none is given, returns all users in this repository.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Get a user", description = "Returns a user or users if no repository is given. If a repository is given, only returns users for that repository will be returned, if no userId is given returns all users in this repository.")
	@GetMapping("/get")
	public ResponseEntity<RestResponse<List<JsonUser>>> getUsers(@Parameter(description = "The userId to search for, if none is given, returns all users in the repository.") @RequestParam(value = "userId", required = false) String userId) {
		try{
			
			UserId id = UserId.of(userId);
			List<JsonUser> jsonUsers;
			
			if(id.isAllUsers()){
				jsonUsers = userService.getUsers().stream().map(JsonUser::new).toList();
			} else {
				jsonUsers = List.of(new JsonUser(userService.getUser(UserId.of(userId))));
			}
			
			return RestResponse.success("", jsonUsers).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while getting users ", e);
			return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Sets a user's role
	 *
	 * @param userId The users id to set the role for.
	 * @param role The role to set.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Removes user Role", description = "Removes a role from a user")
	@PostMapping("/role/remove")
	public ResponseEntity<RestResponse<Void>> removeRole(@Parameter(description = "The users id to set the role for.") @RequestParam("userId") String userId,
														 @Parameter(description = "The role to set.") @RequestParam("role") Role role) {
		try{
			userService.removeRole(UserId.of(userId), role);
			return RestResponse.<Void>success("Set role '%s' for '%s'".formatted(role, userId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Add user Role", description = "Adds a Role to a User")
	@PostMapping("/role/add")
	public ResponseEntity<RestResponse<Void>> addRole(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
													  @Parameter(description = "The users id to set the role for.") @RequestParam("userId") String userId,
													  @Parameter(description = "The role to set.") @RequestParam("role") Role role) {
		try{
			userService.addRole(UserId.of(userId), role);
			return RestResponse.<Void>success("Set role '%s' for '%s' in '%s'".formatted(role, userId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Adds a new user
	 *
	 * @param userId The user's id.
	 * @param password The user's password.
	 * @param groupIds The user's group.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Adds a new user", description = "Adds a new user to the system. If a repository is given, only adds the user to that repository. If none is given, adds the user to all repositories.")
	@PostMapping("/add")
	public ResponseEntity<RestResponse<Void>> addUser(@Parameter(description = "The user's id.") @RequestParam("userId") String userId,
													  @Parameter(description = "The user's password.") @RequestParam("password") String password,
													  @Parameter(description = "The user's group.") @RequestParam(value = "groupIds", required = false) Set<String> groupIds,
													  @Parameter(description = "The user's role.") @RequestParam(value = "role", required = false) Set<Role> roles) {
		try{
			if(groupIds == null){
				groupIds = Set.of();
			}
			Set<GroupId> groupIdSet = groupIds.stream().map(GroupId::of).collect(Collectors.toSet());
			
			userService.addUser(new UserProfile(UserId.of(userId), password, groupIdSet, roles));
			return RestResponse.<Void>success("Added user '%s'".formatted(userId), null).toResponse();
		} catch(ClientException e){//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Removes a user
	 *
	 * @param userId The users id to remove.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Removes a User", description = "Removes a user from the system.")
	@PostMapping("/remove")
	public ResponseEntity<RestResponse<Void>> deleteUser(@Parameter(description = "The users id to remove.") @RequestParam("userId") String userId) {
		try{
			userService.removeUser(UserId.of(userId));
			return RestResponse.<Void>success("Deleted user '%s'".formatted(userId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Adds a permission to a user
	 *
	 * @param repoId The repoId to search in.
	 * @param userId The users id to add the permission to.
	 * @param path The path to add the permission from.
	 * @param type The permission to add.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Adds a permission to a user", description = "Adds a permission to a user.")
	@PostMapping("permission/add")
	public ResponseEntity<RestResponse<Void>> addUserPermission(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
																@Parameter(description = "The users id to add the permission to.") @RequestParam("userId") String userId,
																@Parameter(description = "The path to add the permission from.") @RequestParam("path") String path,
																@Parameter(description = "The permission to add.") @RequestParam("permission") PermissionType type) {
		try{
			Permission<UserId> permission = new Permission<>(UserId.of(userId), type, TargetPath.of(path), RepoId.of(repoId));
			permissionService.addPermissionToUser(RepoId.of(repoId), permission);
			return RestResponse.<Void>success("Added permission '%s' with path '%s' to '%s' in '%s'".formatted(path, type, userId, repoId), null)
							   .toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Removes a permission from a user
	 *
	 * @param repoId The repoId to search in.
	 * @param userId The users id to remove.
	 * @param path The path to remove the permission from.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Removes a permission from a user", description = "Removes a permission from a user.")
	@PostMapping("permission/remove")
	public ResponseEntity<RestResponse<Void>> removeUserPermission(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
																   @Parameter(description = "The users id to remove.") @RequestParam("userId") String userId,
																   @Parameter(description = "The path to remove the permission from.") @RequestParam("path") String path) {
		try{
			permissionService.removePermissionFromUser(RepoId.of(repoId), UserId.of(userId), TargetPath.of(path));
			return RestResponse.<Void>success("Removed permission '%s' from '%s' in '%s'".formatted(path, userId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Updates a permission in a user
	 *
	 * @param repoId The repoId to search in.
	 * @param userId The users id to update.
	 * @param path The path to update the permission for.
	 * @param type The new permission type.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Updates a permission in a user", description = "Updates a permission in a user.")
	@PostMapping("permission/update")
	public ResponseEntity<RestResponse<Void>> updateUserPermission(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
																   @Parameter(description = "The users id to update.") @RequestParam("userId") String userId,
																   @Parameter(description = "The path to update the permission for.") @RequestParam("path") String path,
																   @Parameter(description = "The new permission type.") @RequestParam("type") PermissionType type) {
		try{
			Permission<UserId> permission = new Permission<>(UserId.of(userId), type, TargetPath.of(path), RepoId.of(repoId));
			permissionService.updatePermissionForUser(RepoId.of(repoId), permission);
			return RestResponse.<Void>success("Updated permission '%s' with path '%s' to '%s' in '%s'".formatted(path, type, userId, repoId), null)
							   .toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	/**
	 * Gets the permissions for a user
	 *
	 * @param repoId The repoId to search in.
	 * @param userId The users id to get the permissions for.
	 * @return {@link RestResponse}
	 */
	@Operation(summary = "Gets the permissions for a user", description = "Gets the permissions for a user.")
	@GetMapping("permission/get")
	public ResponseEntity<RestResponse<List<JsonPermission>>> getPermissions(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
																			 @Parameter(description = "The users id to get the permissions for.") @RequestParam("userId") String userId) {
		try{
			Set<Permission<UserId>> permissions = permissionService.getPermissionsForUser(RepoId.of(repoId), UserId.of(userId));
			return RestResponse.success("", permissions.stream().map(JsonPermission::new).toList()).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonPermission>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<List<JsonPermission>>error(e.getMessage()).toResponse();
		}
	}
	
}
