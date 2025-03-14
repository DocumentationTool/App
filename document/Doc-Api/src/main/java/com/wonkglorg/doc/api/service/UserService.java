package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
public class UserService {

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
     * @throws NotaRepoException if the repo does not exist
     * @throws NotaUserException if the user does not exist or null is given and allowNull is false
     */
    public UserId validateUserId(RepoId repoId, String user, boolean allowNull) throws NotaRepoException, NotaUserException {
        if (user == null && allowNull) {
            return UserId.ALL_USERS;
        }

        if (user == null) {
            throw new NotaUserException(repoId, "User id is not allowed to be null!");
        }

        UserId userId = new UserId(user);
        if (!repoService.getRepo(repoId).getDatabase().userExists(userId)) {
            throw new NotaUserException(repoId, "User '%s' does not exist".formatted(userId));
        }

        return userId;
    }


    public void validateUserId(RepoId repoId, UserId userId) throws InvalidUserException {
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
     * @throws NotaRepoException if the repo does not exist
     * @throws NotaUserException if the user does not exist
     */
    public UserId validateUserId(RepoId repoId, String user) throws NotaRepoException, NotaUserException {
        return validateUserId(repoId, user, false);
    }

    /**
     * Gets all users from a specified group
     *
     * @param groupId the group to look for
     * @return the users in the group
     */
    public List<UserId> getUsersFromGroup(RepoId repoId, GroupId groupId) {
        try {
            return QueryDatabaseResponse.success(repoId, getRepo(repoId).getDatabase().getUsersFromGroup(groupId));
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.fail(null, e);
        }
    }

    public QueryDatabaseResponse<List<GroupId>> getGroupsFromUser(RepoId repoId, UserId userId) {
        try {
            return QueryDatabaseResponse.success(repoId, getRepo(repoId).getDatabase().getGroupsFromUser(userId));
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.fail(null, e);
        }
    }

    //---- User ----

    /**
     * Creates a new user in the database
     *
     * @param repoId   the id of the repository (can be null to create the user in all missing repositories)
     * @param userId   the username of the user
     * @param password the password of the user
     * @return the response
     */
    public UpdateDatabaseResponse createUser(RepoId repoId, UserId userId, String password) {
        if (repoService.isValidRepo(repoId)) {
            return UpdateDatabaseResponse.fail(repoId, new NotaRepoException(repoId, "Repo '%s' does not exist".formatted(repoId)));
        }

        return repoService.getRepo(repoId).getDatabase().createUser(userId, password);
    }

    /**
     * Gets a user by their id
     *
     * @param repoId the id of the repository
     * @param userId the id of the user
     * @return the user
     */
    public List<UserProfile> getUsers(RepoId repoId, UserId userId) throws NotaUserException, NotaRepoException {
        return repoService.getRepo(repoId).getDatabase().getUsers(userId);
    }

    /**
     * Gets a user by their id
     *
     * @param repoId the id of the repository
     * @param userId the id of the user
     * @return the user
     * @throws NotaUserException if the user does not exist
     * @throws NotaRepoException if the repository does not exist
     */
    public List<UserProfile> getUsers(String repoId, String userId) throws NotaUserException, NotaRepoException {
        RepoId repo = repoService.validateRepoId(repoId);
        UserId user = repoService.validateUserId(repo, userId);
        return getUsers(repo, user);
    }

    /**
     * Deletes a user from the database
     *
     * @param repoId the id of the repository
     * @param userId the userId of the user to delete
     * @return the response
     */
    public UpdateDatabaseResponse deleteUser(RepoId repoId, UserId userId) {
        return repoService.getRepo(repoId).getDatabase().deleteUser(userId);
    }

    public boolean userExists(RepoId repoId, UserId userId) {
        List<UserProfile> users = repoService.getRepo(repoId).getDatabase().getUsers(userId);
        return users != null && !users.isEmpty();
    }

    public UserId validateUser(RepoId repoId, String userId) {
        UserId id = new UserId(userId);
        if (!repoService.getRepo(repoId).getDatabase().userExists(id)) {
            throw new NotaUserException(repoId, "User '%s' does not exist".formatted(userId));
        }
        return id;
    }

}