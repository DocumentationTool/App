package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.UserCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
public class UserService implements UserCalls{
	
	private final RepoService repoService;
	private final GroupService groupService;
	
	public UserService(@Lazy RepoService repoService, @Lazy GroupService groupService) {
		this.repoService = repoService;
		this.groupService = groupService;
	}
	
	/**
	 * Validates if a repo id is valid, if null is given it will return the ALL_REPOS id
	 *
	 * @param repoId the repo id to validate
	 * @param user the user id to validate
	 * @param allowNull if null is allowed
	 * @return the user id
	 */
	public UserId validateUserId(RepoId repoId, String user, boolean allowNull) throws ClientException {
		if(user == null && allowNull){
			return UserId.ALL_USERS;
		}
		
		if(user == null){
			throw new InvalidUserException("User id is not allowed to be null!");
		}
		
		UserId userId = UserId.of(user);
		if(!userExists(repoId, userId)){
			throw new InvalidUserException("User '%s' does not exist".formatted(userId));
		}
		
		return userId;
	}
	
	public void validateUserId(RepoId repoId, UserId userId) throws InvalidUserException, InvalidRepoException {
		if(!repoService.getRepo(repoId).getDatabase().userFunctions().userExists(repoId, userId)){
			throw new InvalidUserException("User '%s' does not exist in '%s'".formatted(userId, repoId));
		}
	}
	
	/**
	 * Validates if a repo id is valid, if null is given throws an error
	 *
	 * @param repoId the repo id to validate
	 * @param user the user id to validate
	 * @return the user id
	 */
	public UserId validateUserId(RepoId repoId, String user) throws ClientException {
		return validateUserId(repoId, user, false);
	}
	
	//---- User ----
	
	@Override
	public boolean addUser(RepoId repoId, UserProfile user) throws ClientException, CoreSqlException {
		repoService.validateRepoId(repoId);
		if(userExists(repoId, user.getId())){
			throw new ClientException("User with id '%s' already exists".formatted(user.getId()));
		}
		
		for(GroupId groupId : user.getGroups()){
			if(!groupService.groupExists(repoId, groupId)){
				throw new ClientException("Group with id '%s' does not exist".formatted(groupId));
			}
		}
		
		return repoService.getRepo(repoId).getDatabase().userFunctions().addUser(repoId, user);
		
	}
	
	@Override
	public boolean removeUser(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException, CoreSqlException {
		repoService.validateRepoId(repoId);
		validateUser(repoId, userId);
		return repoService.getRepo(repoId).getDatabase().userFunctions().removeUser(repoId, userId);
	}
	
	@Override
	public List<UserProfile> getUsers(RepoId repoId, UserId userId) throws InvalidRepoException {
		return repoService.getRepo(repoId).getDatabase().userFunctions().getUsers(repoId, userId);
	}
	
	@Override
	public boolean addPermissionToUser(RepoId repoId, Permission<UserId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		validateUser(repoId, permission.id());
		if(repoService.getRepo(repoId).getDatabase().userFunctions().userHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("Permission '%s' already exists for '%s' in '%s'".formatted(permission.getPath(), permission.id(), repoId));
		}
		return repoService.getRepo(repoId).getDatabase().userFunctions().addPermissionToUser(repoId, permission);
	}
	
	@Override
	public boolean removePermissionFromUser(RepoId repoId, UserId userId, TargetPath path) throws ClientException {
		repoService.validateRepoId(repoId);
		validateUser(repoId, userId);
		if(!repoService.getRepo(repoId).getDatabase().userFunctions().userHasPermission(userId, path)){
			throw new ClientException("User '%s' does not have permission for path '%s'".formatted(userId, path));
		}
		return repoService.getRepo(repoId).getDatabase().userFunctions().removePermissionFromUser(repoId, userId, path);
	}
	
	@Override
	public boolean updatePermissionForUser(RepoId repoId, Permission<UserId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		validateUser(repoId, permission.id());
		
		if(!repoService.getRepo(repoId).getDatabase().userFunctions().userHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("User '%s' does not have permission for path '%s'".formatted(permission.id(), permission.getPath()));
		}
		
		if(repoService.getRepo(repoId).getDatabase().userFunctions().userHasPermission(permission.id(),
				permission.getPath(),
				permission.getPermission())){
			throw new ClientException("User '%s' already has the permission '%s' for path '%s'".formatted(permission.id(),
					permission.getPermission(),
					permission.getPath().toString()));
		}
		return repoService.getRepo(repoId).getDatabase().userFunctions().updatePermissionForUser(repoId, permission);
	}
	
	/**
	 * Gets a user by their id
	 *
	 * @param repoId the id of the repository
	 * @param userId the id of the user
	 * @return the user
	 */
	@Override
	public UserProfile getUser(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException {
		validateUser(repoId, userId);
		return repoService.getRepo(repoId).getDatabase().userFunctions().getUser(repoId, userId);
	}
	
	@Override
	public boolean userExists(RepoId repoId, UserId userId) throws InvalidRepoException {
		try{
			var users = repoService.getRepo(repoId).getDatabase().userFunctions().getUser(repoId, userId);
			return users != null;
		} catch(InvalidUserException e){
			return false;
		}
	}
	
	/**
	 * Validates if a user exists
	 *
	 * @param repoId the repo id
	 * @param userId the user id
	 */
	public void validateUser(RepoId repoId, UserId userId) throws InvalidUserException, InvalidRepoException {
		if(!userExists(repoId, userId)){
			throw new InvalidUserException("User '%s' does not exist".formatted(userId));
		}
	}
	
}
