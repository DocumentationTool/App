package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionService{
	
	private final UserService userService;
	private final GroupService groupService;
	
	public PermissionService(@Lazy UserService userService, GroupService groupService) {
		this.userService = userService;
		this.groupService = groupService;
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
	public List<Resource> filterResources(RepoId repoId, UserId userId, List<Resource> resources) throws InvalidRepoException, InvalidUserException {
		UserProfile user = userService.getUser(repoId, userId);
		List<Group> groupsFromUser = groupService.getGroupsFromUser(repoId, userId);
		Set<Permission<UserId>> permissions = user.getPermissionsAsSet();
		Set<Permission<GroupId>> groupPermissions = new HashSet<>();
		for(Group group : groupsFromUser){
			//on conflict use the one that allows or denies?
			groupPermissions.addAll(group.getPermissions().values());
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
	
}
