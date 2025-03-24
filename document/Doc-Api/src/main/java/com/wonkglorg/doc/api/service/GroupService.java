package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Component responsible for handling Group related operations.
 */
@Component
@Service
public class GroupService implements GroupCalls{
	private final RepoService repoService;
	private final UserService userService;
	
	// Lazy dependency injection to avoid circular dependencies
	public GroupService(@Lazy RepoService repoService, @Lazy UserService userService) {
		this.repoService = repoService;
		this.userService = userService;
	}
	
	@Override
	public boolean groupExists(RepoId repoId, GroupId groupId) throws InvalidRepoException {
		repoService.validateRepoId(repoId);
		return repoService.getRepo(repoId).getDatabase().userFunctions().groupExists(repoId, groupId);
	}
	
	@Override
	public boolean userInGroup(RepoId repoId, GroupId groupId, UserId userId) throws InvalidRepoException {
		return repoService.getRepo(repoId).getDatabase().userFunctions().userInGroup(repoId, groupId, userId);
	}
	
	@Override
	public boolean addGroup(RepoId repoId, Group group) throws InvalidRepoException, CoreException, InvalidGroupException {
		repoService.validateRepoId(repoId);
		if(groupExists(repoId, group.getId())){
			throw new InvalidGroupException("Group with id '%s' already exists in '%s'".formatted(group.getId(), repoId));
		}
		return repoService.getRepo(repoId).getDatabase().userFunctions().addGroup(repoId, group);
	}
	
	@Override
	public boolean removeGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException, InvalidGroupException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		return repoService.getRepo(repoId).getDatabase().userFunctions().removeGroup(repoId, groupId);
	}
	
	@Override
	public List<Group> getGroups(RepoId repoId, GroupId groupId) throws InvalidRepoException {
		repoService.validateRepoId(repoId);
		return repoService.getRepo(repoId).getDatabase().userFunctions().getGroups(repoId, groupId);
	}
	
	@Override
	public Group renameGroup(RepoId repoId, GroupId groupId, String newName) throws CoreException, InvalidRepoException, InvalidGroupException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		return repoService.getRepo(repoId).getDatabase().userFunctions().renameGroup(repoId, groupId, newName);
	}
	
	@Override
	public boolean addUserToGroup(RepoId repoId, GroupId groupId, UserId userId)
			throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		userService.validateUser(repoId, userId);
		
		if(repoService.getRepo(repoId).getDatabase().userFunctions().userInGroup(repoId, groupId, userId)){
			throw new InvalidUserException("User with id '%s' is already in group '%s'".formatted(userId, groupId));
		}
		
		return repoService.getRepo(repoId).getDatabase().userFunctions().addUserToGroup(repoId, groupId, userId);
	}
	
	@Override
	public boolean removeUserFromGroup(RepoId repoId, GroupId groupId, UserId userId)
			throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		userService.validateUser(repoId, userId);
		
		if(!repoService.getRepo(repoId).getDatabase().userFunctions().userInGroup(repoId, groupId, userId)){
			throw new InvalidUserException("User with id '%s' is not in group '%s'".formatted(userId, groupId));
		}
		
		return repoService.getRepo(repoId).getDatabase().userFunctions().removeUserFromGroup(repoId, groupId, userId);
	}
	
	@Override
	public boolean addPermissionToGroup(RepoId repoId, Permission<GroupId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, permission.id())){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(permission.id()));
		}
		
		if(repoService.getRepo(repoId).getDatabase().userFunctions().groupHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("Group with id '%s' already has permission for path '%s'".formatted(permission.id(),
					permission.getPath().toString()));
		}
		
		return repoService.getRepo(repoId).getDatabase().userFunctions().addPermissionToGroup(repoId, permission);
	}
	
	@Override
	public boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, TargetPath path) throws CoreException, ClientException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		if(!repoService.getRepo(repoId).getDatabase().userFunctions().groupHasPermission(groupId, path)){
			throw new ClientException("Group with id '%s' does not have permission for path '%s'".formatted(groupId, path));
		}
		
		return repoService.getRepo(repoId).getDatabase().userFunctions().removePermissionFromGroup(repoId, groupId, path);
	}
	
	@Override
	public boolean updatePermissionForGroup(RepoId repoId, Permission<GroupId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		if(!groupExists(repoId, permission.id())){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(permission.id()));
		}
		
		if(!repoService.getRepo(repoId).getDatabase().userFunctions().groupHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("Group with id '%s' does not have permission for path '%s'".formatted(permission.id(),
					permission.getPath().toString()));
		}
		
		if(repoService.getRepo(repoId).getDatabase().userFunctions().groupHasPermission(permission.id(),
				permission.getPath(),
				permission.getPermission())){
			throw new ClientException("Group with id '%s' already has the permission '%s' for path '%s'".formatted(permission.id(),
					permission.getPermission(),
					permission.getPath().toString()));
		}
		
		return repoService.getRepo(repoId).getDatabase().userFunctions().updatePermissionForGroup(repoId, permission);
	}
	
	@Override
	public List<UserProfile> getUsersFromGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException {
		return repoService.getRepo(repoId).getDatabase().userFunctions().getUsersFromGroup(repoId, groupId);
	}
	
	@Override
	public List<Group> getGroupsFromUser(RepoId repoId, UserId userId) throws InvalidRepoException {
		return repoService.getRepo(repoId).getDatabase().userFunctions().getGroupsFromUser(repoId, userId);
	}
}
