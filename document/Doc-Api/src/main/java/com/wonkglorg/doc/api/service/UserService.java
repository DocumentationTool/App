package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.interfaces.UserCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
public class UserService implements GroupCalls, UserCalls {

    private final RepoService repoService;

    public UserService(@Lazy RepoService repoService) {
        this.repoService = repoService;
    }

    /**
     * Validates if a repo id is valid, if null is given it will return the ALL_REPOS id
     *
     * @param repoId    the repo id to validate
     * @param user      the user id to validate
     * @param allowNull if null is allowed
     * @return the user id
     */
    public UserId validateUserId(RepoId repoId, String user, boolean allowNull) throws ClientException {
        if (user == null && allowNull) {
            return UserId.ALL_USERS;
        }

        if (user == null) {
            throw new InvalidUserException("User id is not allowed to be null!");
        }

        UserId userId = UserId.of(user);
        if (!repoService.getRepo(repoId).getDatabase().userExists(userId)) {
            throw new InvalidUserException("User '%s' does not exist".formatted(userId));
        }

        return userId;
    }

    public void validateUserId(RepoId repoId, UserId userId) throws InvalidUserException, InvalidRepoException {
        if (!repoService.getRepo(repoId).getDatabase().userExists(userId)) {
            throw new InvalidUserException("User '%s' does not exist in '%s'".formatted(userId, repoId));
        }
    }

    /**
     * Validates if a repo id is valid, if null is given throws an error
     *
     * @param repoId the repo id to validate
     * @param user   the user id to validate
     * @return the user id
     */
    public UserId validateUserId(RepoId repoId, String user) throws ClientException {
        return validateUserId(repoId, user, false);
    }

    /**
     * Gets all users from a specified group
     *
     * @param groupId the group to look for
     * @return the users in the group
     */
    public List<UserProfile> getUsersFromGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        return repoService.getRepo(repoId).getDatabase().getUsersFromGroup(groupId);
    }

    public List<Group> getGroupsFromUser(RepoId repoId, UserId userId) throws InvalidRepoException {
        return repoService.getRepo(repoId).getDatabase().getGroupsFromUser(userId);
    }

    //---- User ----


    @Override
    public boolean addUser(RepoId repoId, UserProfile user) throws ClientException, CoreSqlException {
        repoService.validateRepoId(repoId);
        if (userExists(repoId, user.getId())) {
            throw new ClientException("User with id '%s' already exists".formatted(user.getId()));
        }

        for (GroupId groupId : user.getGroups()) {
            if (!groupExists(repoId, groupId)) {
                throw new ClientException("Group with id '%s' does not exist".formatted(groupId));
            }
        }

        return repoService.getRepo(repoId).getDatabase().addUser(repoId, user);

    }

    @Override
    public boolean removeUser(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException, CoreSqlException {
        repoService.validateRepoId(repoId);
        validateUser(repoId, userId);
        return repoService.getRepo(repoId).getDatabase().removeUser(repoId, userId);
    }

    /**
     * Gets a user by their id
     *
     * @param repoId the id of the repository
     * @param userId the id of the user
     * @return the user
     */
    @Override
    public List<UserProfile> getUsers(RepoId repoId, UserId userId) throws InvalidRepoException {
        return repoService.getRepo(repoId).getDatabase().getUsers(repoId, userId);
    }

    @Override
    public boolean updateUser(RepoId repoId, UserId userId, UserProfile user) {
        return false;
    }

    /**
     * Gets a user by their id
     *
     * @param repoId the id of the repository
     * @param userId the id of the user
     * @return the user
     */
    public UserProfile getUser(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException {
        validateUser(repoId, userId);
        return repoService.getRepo(repoId).getDatabase().getUsers(repoId, userId).get(0);
    }

    /**
     * Gets a user by their id
     *
     * @param repoId the id of the repository
     * @param userId the id of the user
     * @return the user
     */
    public List<UserProfile> getUsers(String repoId, String userId) throws ClientException {
        RepoId repo = repoService.validateRepoId(repoId);
        return getUsers(repo, UserId.of(userId));
    }

    public boolean userExists(RepoId repoId, UserId userId) throws InvalidRepoException {
        List<UserProfile> users = repoService.getRepo(repoId).getDatabase().getUsers(repoId, userId);
        return users != null && !users.isEmpty();
    }

    /**
     * Validates if a user exists
     *
     * @param repoId the repo id
     * @param userId the user id
     * @return the user id
     */
    public UserId validateUser(RepoId repoId, String userId) throws InvalidUserException, InvalidRepoException {
        UserId id = UserId.of(userId);
        validateUser(repoId, id);
        return id;
    }

    /**
     * Validates if a user exists
     *
     * @param repoId the repo id
     * @param userId the user id
     */
    public void validateUser(RepoId repoId, UserId userId) throws InvalidUserException, InvalidRepoException {
        if (!repoService.getRepo(repoId).getDatabase().userExists(userId)) {
            throw new InvalidUserException("User '%s' does not exist".formatted(userId));
        }
    }

    public boolean groupExists(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        repoService.validateRepoId(repoId);
        return repoService.getRepo(repoId).getDatabase().groupExists(groupId);
    }

    @Override
    public boolean addGroup(RepoId repoId, Group group) throws InvalidRepoException, CoreException, InvalidGroupException {
        repoService.validateRepoId(repoId);
        if (groupExists(repoId, group.getId())) {
            throw new InvalidGroupException("Group with id '%s' already exists in '%s'".formatted(group.getId(), repoId));
        }
        return repoService.getRepo(repoId).getDatabase().addGroup(repoId, group);
    }

    @Override
    public boolean removeGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException, InvalidGroupException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        return repoService.getRepo(repoId).getDatabase().removeGroup(repoId, groupId);
    }

    public List<Group> getGroups(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        repoService.validateRepoId(repoId);
        return repoService.getRepo(repoId).getDatabase().getGroups(repoId, groupId);
    }
}
