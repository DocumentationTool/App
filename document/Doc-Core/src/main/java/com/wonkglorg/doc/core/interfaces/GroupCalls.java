package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.user.Group;

import java.util.List;

/**
 * A common interface referencing all group related calls
 */
public interface GroupCalls {


    /**
     * Checks if a group exists in a repo
     *
     * @param repoId  the repo to check in
     * @param groupId the group to check for
     * @param userId  the user to check for
     * @return true if the group exists
     */
    boolean userInGroup(RepoId repoId, GroupId groupId, UserId userId) throws InvalidRepoException;

    /**
     * Adds a group to a repo
     *
     * @param repoId the repo to add the group to
     * @param group  the group to add
     * @return true if the group was added
     * @throws InvalidRepoException  if the repo is invalid
     * @throws CoreException         if the group could not be added
     * @throws InvalidGroupException if the group is invalid
     */
    boolean addGroup(RepoId repoId, Group group) throws InvalidRepoException, CoreException, InvalidGroupException;

    /**
     * Removes a group from a repo
     *
     * @param repoId  the repo to remove the group from
     * @param groupId the group to remove
     * @return true if the group was removed
     * @throws CoreException         if the group could not be removed
     * @throws InvalidRepoException  if the repo is invalid
     * @throws InvalidGroupException if the group is invalid
     */
    boolean removeGroup(RepoId repoId, GroupId groupId) throws CoreException, InvalidRepoException, InvalidGroupException;

    /**
     * Gets all groups in a repo
     *
     * @param repoId  the repo to get the groups from
     * @param groupId the group to get the groups from
     * @return a list of groups
     */
    List<Group> getGroups(RepoId repoId, GroupId groupId) throws InvalidRepoException;


    /**
     * Updates a group
     *
     * @param repoId  the repo to update the group in
     * @param groupId the group to update
     * @param newName the updated group
     * @return the updated group
     * @throws CoreException         if the group could not be updated
     * @throws InvalidRepoException  if the repo is invalid
     * @throws InvalidGroupException if the group is invalid
     */
    Group renameGroup(RepoId repoId, GroupId groupId, String newName) throws CoreException, InvalidRepoException, InvalidGroupException;

    /**
     * Adds a user to a group
     *
     * @param repoId  the repo to add the user to
     * @param groupId the group to add the user to
     * @param userId  the user to add to the group
     * @return true if the user was added to the group
     * @throws CoreException         if the user could not be added to the group
     * @throws InvalidRepoException  if the repo is invalid
     * @throws InvalidGroupException if the group is invalid
     */
    boolean addUserToGroup(RepoId repoId, GroupId groupId, UserId userId) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException;


    /**
     * Removes a user from a group
     *
     * @param repoId  the repo to remove the user from
     * @param groupId the group to remove the user from
     * @param userId  the user to remove from the group
     * @return true if the user was removed from the group
     * @throws CoreException         if the user could not be removed from the group
     * @throws InvalidRepoException  if the repo is invalid
     * @throws InvalidGroupException if the group is invalid
     */
    boolean removeUserFromGroup(RepoId repoId, GroupId groupId, UserId userId) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException;


    /**
     * Adds a permission to a group
     * @param repoId the repo to add the permission to
     * @param groupId the group to add the permission to
     * @param permission the permission to add
     * @return
     * @throws CoreException
     * @throws InvalidRepoException
     * @throws InvalidGroupException
     * @throws InvalidUserException
     */
    boolean addPermissionToGroup(RepoId repoId, GroupId groupId, Permission<GroupId> permission) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException;

    /**
     * Removes a permission from a group
     * @param repoId the repo to remove the permission from
     * @param groupId the group to remove the permission from
     * @param permission the permission to remove
     * @return true if the permission was removed
     * @throws CoreException
     * @throws InvalidRepoException
     * @throws InvalidGroupException
     * @throws InvalidUserException
     */
    boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, Permission<GroupId> permission) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException;
}
