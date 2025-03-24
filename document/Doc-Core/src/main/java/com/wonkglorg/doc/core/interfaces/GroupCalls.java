package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.List;
import java.util.Set;

/**
 * A common interface referencing all group related calls
 */
public interface GroupCalls{
	
	/**
	 * Checks if a group exists in a repository.
	 *
	 * @param repoId The repository id.
	 * @param groupId The group id.
	 * @return True if the group exists, false otherwise.
	 * @throws InvalidRepoException If the repository does not exist.
	 */
	boolean groupExists(GroupId groupId);
	
	/**
	 * Checks if a group exists in a repo
	 *
	 * @param repoId the repo to check in
	 * @param groupId the group to check for
	 * @param userId the user to check for
	 * @return true if the group exists
	 */
	boolean userInGroup(GroupId groupId, UserId userId) throws InvalidUserException, InvalidGroupException;
	
	/**
	 * Adds a group to a repo
	 *
	 * @param repoId the repo to add the group to
	 * @param group the group to add
	 * @return true if the group was added
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws CoreException if the group could not be added
	 * @throws InvalidGroupException if the group is invalid
	 */
	boolean addGroup(Group group) throws CoreException, InvalidGroupException;
	
	/**
	 * Removes a group from a repo
	 *
	 * @param repoId the repo to remove the group from
	 * @param groupId the group to remove
	 * @return true if the group was removed
	 * @throws CoreException if the group could not be removed
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidGroupException if the group is invalid
	 */
	boolean removeGroup(GroupId groupId) throws CoreException, InvalidGroupException;
	
	/**
	 * Gets all groups
	 *
	 * @return a list of groups
	 */
	List<Group> getGroups();
	
	/**
	 * Gets a group
	 *
	 * @param groupId the group to get
	 * @return the group
	 */
	Group getGroup(GroupId groupId) throws InvalidGroupException;
	
	/**
	 * Updates a group
	 *
	 * @param repoId the repo to update the group in
	 * @param groupId the group to update
	 * @param newName the updated group
	 * @return the updated group
	 * @throws CoreException if the group could not be updated
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidGroupException if the group is invalid
	 */
	Group renameGroup(GroupId groupId, String newName) throws CoreException, InvalidGroupException;
	
	/**
	 * Adds a user to a group
	 *
	 * @param repoId the repo to add the user to
	 * @param groupId the group to add the user to
	 * @param userId the user to add to the group
	 * @return true if the user was added to the group
	 * @throws CoreException if the user could not be added to the group
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidGroupException if the group is invalid
	 */
	boolean addUserToGroup(GroupId groupId, UserId userId) throws CoreException, ClientException;
	
	/**
	 * Removes a user from a group
	 *
	 * @param repoId the repo to remove the user from
	 * @param groupId the group to remove the user from
	 * @param userId the user to remove from the group
	 * @return true if the user was removed from the group
	 * @throws CoreException if the user could not be removed from the group
	 * @throws InvalidRepoException if the repo is invalid
	 * @throws InvalidGroupException if the group is invalid
	 */
	boolean removeUserFromGroup(GroupId groupId, UserId userId) throws CoreException, InvalidGroupException, InvalidUserException;
	
	Set<Group> getGroupsFromUser(UserId userId) throws InvalidUserException;
	
	Set<UserProfile> getUsersFromGroup(GroupId groupId) throws InvalidGroupException;
}
