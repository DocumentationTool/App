package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.List;

public interface UserCalls{
	
	/**
	 * Creates a new user in the database
	 *
	 * @param repoId the id of the repository (can be null to create the user in all missing repositories)
	 * @param user the user to create
	 * @return the response
	 */
	boolean addUser(RepoId repoId, UserProfile user) throws ClientException, CoreSqlException;
	
	/**
	 * Removes a user from the database
	 * @param repoId the id of the repository (can be null to remove the user from all repositories)
	 * @param userId the user to remove
	 * @return the response
	 * @throws CoreSqlException if the user could not be removed
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidUserException if the user is invalid
	 */
	boolean removeUser(RepoId repoId, UserId userId) throws CoreSqlException, InvalidRepoException, InvalidUserException;
	
	/**
	 * Gets all users in a repo
	 *
	 * @param repoId the repo to get the users from
	 * @param userId the user to get the users from
	 * @return a list of users
	 */
	List<UserProfile> getUsers(RepoId repoId, UserId userId) throws InvalidRepoException;
	
	/**
	 * Gets a user from the database
	 *
	 * @param repoId the id of the repository
	 * @param userId the id of the user
	 * @return the user
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidUserException if the user is invalid
	 */
	UserProfile getUser(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException;
	
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
	
}
