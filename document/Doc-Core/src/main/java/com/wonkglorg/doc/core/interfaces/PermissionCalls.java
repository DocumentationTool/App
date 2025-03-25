package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;

import java.util.Set;

public interface PermissionCalls{
	
	/**
	 * Adds a permission to a group
	 *
	 * @param repoId the repo to add the permission to
	 * @param groupId the group to add the permission to
	 * @param permission the permission to add
	 * @return
	 * @throws CoreException
	 * @throws InvalidRepoException
	 * @throws InvalidGroupException
	 * @throws InvalidUserException
	 */
	boolean addPermissionToGroup(RepoId repoId, Permission<GroupId> permission) throws CoreException, ClientException;
	
	/**
	 * Removes a permission from a group
	 *
	 * @param repoId the repo to remove the permission from
	 * @param groupId the group to remove the permission from
	 * @param path the path to remove the permission from
	 * @return true if the permission was removed
	 */
	boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, TargetPath path) throws CoreException, ClientException;
	
	/**
	 * Updates a permission in a group
	 *
	 * @param repoId the repo to update the permission in
	 * @param path the path to update the permission for
	 * @param groupId the group to update the permission for
	 * @param type the new permission type
	 * @return true if the permission was updated
	 * @throws CoreException if the permission could not be updated
	 * @throws ClientException if the permission is invalid
	 */
	boolean updatePermissionForGroup(RepoId repoId, Permission<GroupId> permission) throws CoreException, ClientException;
	
	/**
	 * Adds a permission to a group
	 *
	 * @param repoId the repo to add the permission to
	 * @param permission the permission to add
	 * @return true if the permission was added
	 */
	boolean addPermissionToUser(RepoId repoId, Permission<UserId> permission) throws CoreException, ClientException;
	
	/**
	 * Removes a permission from a group
	 *
	 * @param repoId the repo to remove the permission from
	 * @param userId the user to remove the permission from
	 * @param path the path to remove the permission from
	 * @return true if the permission was removed
	 */
	boolean removePermissionFromUser(RepoId repoId, UserId userId, TargetPath path) throws CoreException, ClientException;
	
	/**
	 * Updates a permission in a group
	 *
	 * @param repoId the repo to update the permission in
	 * @param permission the permission to update
	 * @return true if the permission was updated
	 * @throws CoreException if the permission could not be updated
	 * @throws ClientException if the permission is invalid
	 */
	boolean updatePermissionForUser(RepoId repoId, Permission<UserId> permission) throws CoreException, ClientException;
	
	/**
	 * Gets the permissions for a user
	 *
	 * @param repoId the repo
	 * @param userId the user
	 * @return the permissions
	 */
	Set<Permission<UserId>> getPermissionsForUser(RepoId repoId, UserId userId) throws CoreException, ClientException;
	
	/**
	 * Gets the permissions for a group
	 *
	 * @param repoId the repo
	 * @param groupId the group
	 * @return the permissions
	 */
	Set<Permission<GroupId>> getPermissionsForGroup(RepoId repoId, GroupId groupId) throws CoreException, ClientException;
}
