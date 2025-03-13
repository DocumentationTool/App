package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
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
     * Creates a new user in the database
     *
     * @param repoId   the id of the repository
     * @param userId   the username of the user
     * @param password the password of the user
     * @return the response
     */
    public UpdateDatabaseResponse createUser(RepoId repoId, UserId userId, String password) {
        return repoService.getRepo(repoId).getDatabase().addUser(userId, password);
    }

    /**
     * Gets a user by their id
     *
     * @param repoId the id of the repository
     * @param userId the id of the user
     * @return the user
     */
    public List<UserProfile> getUsers(RepoId repoId, UserId userId) {
        return repoService.getRepo(repoId).getDatabase().getUsers(userId);
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
        return users != null || !users.isEmpty();
    }

}