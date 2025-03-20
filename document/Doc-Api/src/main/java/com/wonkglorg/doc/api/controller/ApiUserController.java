package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;
import com.wonkglorg.doc.api.json.JsonGroup;
import com.wonkglorg.doc.api.json.JsonUser;
import com.wonkglorg.doc.api.service.UserService;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles all api user specific requests
 */
@RestController
@RequestMapping(API_USER)
public class ApiUserController implements GroupCalls{
	/**
	 * Logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);
	private final UserService userService;
	
	public ApiUserController(@Lazy UserService userService) {
		this.userService = userService;
	}
	
	//todo:jmd checked
	@Operation(summary = "Get a user", description = "Returns a user or users if no repository is given. If a repository is given, only returns users for that repository will be returned, if no userId is given returns all users in this repository.")
	@GetMapping("/get")
	public ResponseEntity<RestResponse<List<JsonUser>>> getUsers(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
																 @Parameter(description = "The userId to search for, if none is given, returns all users in the repository.") @RequestParam(value = "userId", required = false) String userId) {
		try{
			List<UserProfile> users = userService.getUsers(repoId, userId);
			List<JsonUser> jsonUsers = users.stream().map(JsonUser::new).toList();
			
			return RestResponse.success("", jsonUsers).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while getting users ", e);
			return RestResponse.<List<JsonUser>>error(e.getMessage()).toResponse();
		}
	}
	
	//todo:jmd checked
	@Operation(summary = "Adds a new user", description = "Adds a new user to the system. If a repository is given, only adds the user to that repository. If none is given, adds the user to all repositories.")
	@PutMapping("/add")
	public ResponseEntity<RestResponse<Void>> addUser(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
													  @Parameter(description = "The user's id.") @RequestParam("userId") String userId,
													  @Parameter(description = "The user's password.") @RequestParam("password") String password) {
		try{
			userService.createUser(RepoId.of(repoId), UserId.of(userId), password);
			return RestResponse.<Void>success("Added user '%s' to repo '%s".formatted(userId, repoId), null).toResponse();
		} catch(CoreException e){//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	//todo:jmd checked
	@Operation(summary = "Removes a User", description = "Removes a user from the system.")
	@PutMapping("/remove")
	public ResponseEntity<RestResponse<Void>> deleteUser(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
														 @Parameter(description = "The users id to remove.") @RequestParam("userId") String userId) {
		try{
			userService.deleteUser(RepoId.of(repoId), UserId.of(userId));
			return RestResponse.<Void>success("Deleted user '%s' from repo '%s".formatted(userId, repoId), null).toResponse();
		} catch(CoreException e){//core exceptions are stuff only returned to the client, and isn't an actual error that needs fixing by the coder
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Get groups", description = "Returns a group or groups if no groupId is given.")
	@GetMapping("group/get")
	public ResponseEntity<RestResponse<List<JsonGroup>>> getGroups(@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.") @RequestParam(value = "repoId") String repoId,
																   @Parameter(description = "The groupid to search for, if none is given, returns all groups in the repository.") @RequestParam(value = "groupId", required = false) String groupId) {
		try{
			
			List<Group> users = userService.getGroups(RepoId.of(repoId), GroupId.of(groupId));
			List<JsonGroup> jsonGroups = users.stream().map(JsonGroup::new).toList();
			
			return RestResponse.success(jsonGroups).toResponse();
		} catch(ClientException e){
			return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while getting users ", e);
			return RestResponse.<List<JsonGroup>>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Removes a Group", description = "Removes a group from the system.")
	@PutMapping("group/remove")
	public ResponseEntity<RestResponse<Void>> deleteGroup(@Parameter(description = "The repoId to search in.") @RequestParam("repoId") String repoId,
														  @Parameter(description = "The users id to remove.") @RequestParam("groupId") String groupId) {
		try{
			userService.deleteGroup(RepoId.of(repoId), GroupId.of(groupId));
			return RestResponse.<Void>success("Deleted group '%s' from repo '%s", null).toResponse();
		} catch(InvalidRepoException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	@Operation(summary = "Adds a new Group", description = "Adds a new Group to the repo.")
	@PutMapping("group/add")
	public ResponseEntity<RestResponse<Void>> addGroup(@Parameter(description = "The repoId to add the group to.") @RequestParam(value = "repoId") String repoId,
													   @Parameter(description = "The groupId.") @RequestParam("groupId") String groupId,
													   @Parameter(description = "The groupname.") @RequestParam("groupName") String groupName) {
		try{
			userService.createGroup(RepoId.of(repoId), GroupId.of(groupId), groupName);
			return RestResponse.<Void>success("Added group '%s' to repo '%s".formatted(groupId, repoId), null).toResponse();
		} catch(ClientException e){
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		} catch(Exception e){
			log.error("Error while checking edited state ", e);
			return RestResponse.<Void>error(e.getMessage()).toResponse();
		}
	}
	
	@Override
	public boolean addGroup(RepoId repoId, Group group) {
		return false;
	}
	
	@Override
	public boolean removeGroup(RepoId repoId, GroupId groupId) {
		return false;
	}
	
	@Override
	public List<Group> getGroups(RepoId repoId, GroupId groupId) {
		return List.of();
	}
}
