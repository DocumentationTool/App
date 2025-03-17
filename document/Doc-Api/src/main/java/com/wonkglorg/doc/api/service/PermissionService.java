package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PermissionService{
	
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();
	
	/**
	 * Filters a list of resources based on the permissions of a user if non is given return all resources with permission access Edit
	 *
	 * @param userPermissions the permissions of the user (can be null)
	 * @param groupPermissions the permissions of the groups the user is in (can be null)
	 * @param resources the resources to filter
	 * @return the filtered resources
	 */
	public List<Resource> filterResources(List<Permission<UserId>> userPermissions,
										  List<Permission<GroupId>> groupPermissions,
										  List<Resource> resources) {
		
		List<Path> resourcePaths = resources.stream().map(Resource::resourcePath).collect(Collectors.toList());
		
		Map<Path, PermissionType> filteredResources = filterWithPermissions(userPermissions, groupPermissions, resourcePaths);
		
		//noinspection SimplifyStreamApiCallChains not a good idea as peak can be skipped by the compiler
		return resources.stream().filter(resource -> filteredResources.containsKey(resource.resourcePath())).map(resource -> {
			PermissionType permission = filteredResources.get(resource.resourcePath());
			resource.setPermissionType(permission);
			return resource;
		}).collect(Collectors.toList());
	}
	
	/**
	 * Filters a list of resources based on the permissions of a user if non is given return all resources with permission access Edit
	 *
	 * @param userPermissions the permissions of the user (can be null)
	 * @param groupPermissions the permissions of the groups the user is in (can be null)
	 * @param userId the user id
	 * @param resourcePaths the paths of the resources
	 * @return a map of the resources and their permissions
	 */
	@Cacheable("permissions")
	public Map<Path, PermissionType> filterWithPermissions(List<Permission<UserId>> userPermissions,
														   List<Permission<GroupId>> groupPermissions,
														   List<Path> resourcePaths) {
		Map<String, PermissionType> fullPathsGroup = new HashMap<>();
		TreeMap<String, PermissionType> antPathsGroup = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));
		
		Map<String, PermissionType> fullPathsUser = new HashMap<>();
		TreeMap<String, PermissionType> antPathsUser = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));
		
		if(userPermissions == null && groupPermissions == null){
			return resourcePaths.stream().collect(Collectors.toMap(path -> path, path -> PermissionType.DENY));
		}
		
		// First collect group permissions
		if(groupPermissions != null){
			for(var permission : groupPermissions){
				storePermission(permission, antPathsGroup, fullPathsGroup);
			}
		}
		
		// Then collect user permissions (ensuring they overwrite group permissions)
		if(userPermissions != null){
			for(var permission : userPermissions){
				storePermission(permission, antPathsUser, fullPathsUser);
			}
		}
		
		Map<Path, PermissionType> result = new HashMap<>();
		
		//apply perms
		for(Path resourcePath : resourcePaths){
			String resourceString = resourcePath.toString();
			PermissionType appliedPermission = null;
			
			// 1. Check full paths (user-specific first, then group)
			if(fullPathsUser.containsKey(resourceString)){
				appliedPermission = fullPathsUser.get(resourceString);
			} else if(fullPathsGroup.containsKey(resourceString)){
				appliedPermission = fullPathsGroup.get(resourceString);
			}
			
			// 2. Check ant paths (user-specific first, then group)
			if(appliedPermission == null){
				for(var entry : antPathsUser.entrySet()){
					if(antPathMatcher.match(entry.getKey(), resourceString)){
						appliedPermission = entry.getValue();
						break;
					}
				}
			}
			
			if(appliedPermission == null){
				for(var entry : antPathsGroup.entrySet()){
					if(antPathMatcher.match(entry.getKey(), resourceString)){
						appliedPermission = entry.getValue();
						break;
					}
				}
			}
			
			if(appliedPermission != null){
				result.put(resourcePath, appliedPermission);
			}
		}
		
		return result;
	}
	
	private void storePermission(Permission<?> permission, TreeMap<String, PermissionType> antPaths, Map<String, PermissionType> fullPaths) {
		String path = permission.getPath().path();
		if(antPathMatcher.isPattern(path)){
			antPaths.put(path, permission.getPermission());
		} else {
			fullPaths.put(path, permission.getPermission());
		}
	}
	
}
