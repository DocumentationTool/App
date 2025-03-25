package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.PermissionCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.user.Group;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionService implements PermissionCalls{
	
	private final UserService userService;
	private final RepoService repoService;
	
	public PermissionService(@Lazy UserService userService, RepoService repoService) {
		this.userService = userService;
		this.repoService = repoService;
	}
	
	/**
	 * Filters a list of resources based on the permissions of a user if non is given return all resources with permission access Edit
	 *
	 * @param repoId the repo id
	 * @param userId the user id
	 * @param resources the resources to filter
	 * @return the filtered resources
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidUserException if the user is invalid
	 */
	public List<Resource> filterResources(RepoId repoId, UserId userId, List<Resource> resources) throws ClientException, CoreException {
		repoService.validateRepoId(repoId);
		userService.validateUser(userId);
		Set<Group> groupsFromUser = userService.getGroupsFromUser(userId);
		
		Set<Permission<UserId>> permissions = getPermissionsForUser(repoId, userId);
		Set<Permission<GroupId>> groupPermissions = new HashSet<>();
		for(Group group : groupsFromUser){
			groupPermissions.addAll(getPermissionsForGroup(repoId, group.getId()));
		}
		
		Map<Path, PermissionType> permissionTypeMap = Permission.filterPathsWithPermissions(permissions,
				groupPermissions,
				resources.stream().map(Resource::resourcePath).collect(Collectors.toList()));
		
		for(Resource resource : resources){
			PermissionType permission = permissionTypeMap.get(resource.resourcePath());
			resource.setPermissionType(permission);
		}
		
		return resources;
	}
	
	@Override
	public boolean addPermissionToGroup(RepoId repoId, Permission<GroupId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		if(!userService.groupExists(permission.id())){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(permission.id()));
		}
		
		if(repoService.getRepo(repoId).getDatabase().permissionFunctions().groupHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("Group with id '%s' already has permission for path '%s'".formatted(permission.id(),
					permission.getPath().toString()));
		}
		
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().addPermissionToGroup(repoId, permission);
	}
	
	@Override
	public boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, TargetPath path) throws CoreException, ClientException {
		repoService.validateRepoId(repoId);
		if(!userService.groupExists(groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		if(!repoService.getRepo(repoId).getDatabase().permissionFunctions().groupHasPermission(groupId, path)){
			throw new ClientException("Group with id '%s' does not have permission for path '%s'".formatted(groupId, path));
		}
		
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().removePermissionFromGroup(repoId, groupId, path);
	}
	
	@Override
	public boolean updatePermissionForGroup(RepoId repoId, Permission<GroupId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		if(!userService.groupExists(permission.id())){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(permission.id()));
		}
		
		if(!repoService.getRepo(repoId).getDatabase().permissionFunctions().groupHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("Group with id '%s' does not have permission for path '%s'".formatted(permission.id(),
					permission.getPath().toString()));
		}
		
		if(repoService.getRepo(repoId).getDatabase().permissionFunctions().groupHasPermission(permission.id(),
				permission.getPath(),
				permission.getPermission())){
			throw new ClientException("Group with id '%s' already has the permission '%s' for path '%s'".formatted(permission.id(),
					permission.getPermission(),
					permission.getPath().toString()));
		}
		
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().updatePermissionForGroup(repoId, permission);
	}
	
	@Override
	public boolean addPermissionToUser(RepoId repoId, Permission<UserId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		userService.validateUser(permission.id());
		if(repoService.getRepo(repoId).getDatabase().permissionFunctions().userHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("Permission '%s' already exists for '%s' in '%s'".formatted(permission.getPath(), permission.id(), repoId));
		}
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().addPermissionToUser(repoId, permission);
	}
	
	@Override
	public boolean removePermissionFromUser(RepoId repoId, UserId userId, TargetPath path) throws ClientException {
		repoService.validateRepoId(repoId);
		userService.validateUser(userId);
		if(!repoService.getRepo(repoId).getDatabase().permissionFunctions().userHasPermission(userId, path)){
			throw new ClientException("User '%s' does not have permission for path '%s'".formatted(userId, path));
		}
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().removePermissionFromUser(repoId, userId, path);
	}
	
	@Override
	public boolean updatePermissionForUser(RepoId repoId, Permission<UserId> permission) throws ClientException {
		repoService.validateRepoId(repoId);
		userService.validateUser(permission.id());
		
		if(!repoService.getRepo(repoId).getDatabase().permissionFunctions().userHasPermission(permission.id(), permission.getPath())){
			throw new ClientException("User '%s' does not have permission for path '%s'".formatted(permission.id(), permission.getPath()));
		}
		
		if(repoService.getRepo(repoId).getDatabase().permissionFunctions().userHasPermission(permission.id(),
				permission.getPath(),
				permission.getPermission())){
			throw new ClientException("User '%s' already has the permission '%s' for path '%s'".formatted(permission.id(),
					permission.getPermission(),
					permission.getPath().toString()));
		}
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().updatePermissionForUser(repoId, permission);
	}
	
	@Override
	public Set<Permission<UserId>> getPermissionsForUser(RepoId repoId, UserId userId) throws CoreException, ClientException {
		repoService.validateRepoId(repoId);
		userService.validateUser(userId);
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().getPermissionsForUser(repoId, userId);
	}
	
	@Override
	public Set<Permission<GroupId>> getPermissionsForGroup(RepoId repoId, GroupId groupId) throws CoreException, ClientException {
		repoService.validateRepoId(repoId);
		userService.validateGroup(groupId);
		return repoService.getRepo(repoId).getDatabase().permissionFunctions().getPermissionsForGroup(repoId, groupId);
	}
	
}
