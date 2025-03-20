package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.List;

public interface UserCalls {

    /**
     * Creates a new user in the database
     *
     * @param repoId the id of the repository (can be null to create the user in all missing repositories)
     * @param user the user to create
     * @return the response
     */
    boolean addUser(RepoId repoId, UserProfile user) throws ClientException, CoreSqlException;

    boolean removeUser(RepoId repoId, UserId userId) throws CoreSqlException, InvalidRepoException, InvalidUserException;

    /**
     * Gets all users in a repo
     *
     * @param repoId the repo to get the users from
     * @param userId the user to get the users from
     * @return a list of users
     */
    List<UserProfile> getUsers(RepoId repoId, UserId userId) throws InvalidRepoException;


    boolean updateUser(RepoId repoId, UserId userId, UserProfile user);


}
