package com.wonkglorg.doc.core.db.daos;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;

import java.nio.file.Path;
import java.util.List;

/**
 * Permission related database functions
 */
public interface PermissionFunctions{
	
	List<Permission<UserId>> getPermissionsForUser(UserId userId);
	
	List<Permission<GroupId>> getPermissionsForGroup(GroupId groupId);
	
	PermissionType getPermissionForFolder(UserId userId, Path path);
	
	List<PermissionType> getAllPermissions();
	
}
