package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Service
public class UserService {

    private final RepoService repoService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

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

        UserId userId = new UserId(user);
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

    /**
     * Creates a new user in the database
     *
     * @param repoId   the id of the repository (can be null to create the user in all missing repositories)
     * @param userId   the username of the user
     * @param password the password of the user
     * @return the response
     */
    public boolean createUser(RepoId repoId, UserId userId, String password) throws ClientException, CoreException {
        repoService.validateRepoId(repoId);
        if (userExists(repoId, userId)) {
            throw new CoreException("User with id '%s' already exists".formatted(userId));
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
    public List<UserProfile> getUsers(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException {
        validateUser(repoId, userId);
        return repoService.getRepo(repoId).getDatabase().getUsers(userId);
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
        return repoService.getRepo(repoId).getDatabase().getUsers(userId).get(0);
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
        UserId user = validateUserId(repo, userId);
        return getUsers(repo, user);
    }

    /**
     * Deletes a user from the database
     *
     * @param repoId the id of the repository
     * @param userId the userId of the user to delete
     * @return the response
     */
    public boolean deleteUser(RepoId repoId, UserId userId) throws InvalidRepoException, CoreSqlException, InvalidUserException {
        repoService.validateRepoId(repoId);
        validateUser(repoId, userId);
        return repoService.getRepo(repoId).getDatabase().deleteUser(userId);
    }

    public boolean userExists(RepoId repoId, UserId userId) throws InvalidRepoException {
        List<UserProfile> users = repoService.getRepo(repoId).getDatabase().getUsers(userId);
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
        UserId id = new UserId(userId);
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

    public void createGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException, CoreException {
        repoService.validateRepoId(repoId);
        if (groupExists(repoId, groupId)) {
            throw new CoreException("Group with id '%s' already exists".formatted(groupId));
        }
        repoService.getRepo(repoId).getDatabase().createGroup(groupId);
    }

    public boolean groupExists(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        repoService.validateRepoId(repoId);
        return repoService.getRepo(repoId).getDatabase().groupExists(groupId);
    }

    public void deleteGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException, CoreException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new CoreException("Group with id '%s' does not exist".formatted(groupId));
        }
        repoService.getRepo(repoId).getDatabase().deleteGroup(groupId);
    }


    /**
     * Filters resources based on the permissions of the user without the groups they are in
     *
     * @param id        the user id
     * @param resources the resources to filter
     * @return the filtered resources
     */
    public List<Resource> filterUsingUserPermissions(RepoId repoId, UserId id, List<Resource> resources) throws InvalidUserException, InvalidRepoException {
        UserProfile user = getUser(repoId, id);
        if (user == null) {
            throw new InvalidUserException("User '%s' does not exist".formatted(id));
        }


        //todo:jmd this is where the filtering should happen, checks wether the user has the permission to view the resource or not
        Set<Permission<UserId>> permissions = user.getPermissions();

        for (Resource resource : resources) {
            for (Permission<UserId> permission : permissions) {
                if (permission.getPermission().equals(resource.getPermissionType())) {
                    //something something set new path based on closest matching permission from this one,

                    //example: ** gets overwriten by test/** if the file is located in test, sort by length? does that help
                }
            }
        }


        return resources;
    }


    /**
     * Filters resources based on the permissions of the group
     *
     * @param groupId   the group id
     * @param resources the resources to filter
     * @return the filtered resources
     */
    public List<Resource> filterUsingGroupPermissions(RepoId repoId, GroupId groupId, List<Resource> resources) {
        return resources;
    }


    /**
     * Filters resources based on the permissions of the user and their groups they are in
     *
     * @param userId    the user id
     * @param resources the resources to filter
     * @return the filtered resources
     */
    public List<Resource> filterUsingPermissions(RepoId repoId, UserId userId, List<Resource> resources) {
        Map<String, PermissionType> antPaths = new HashMap<>();
        Map<String, PermissionType> fullPaths = new HashMap<>();

        UserProfile user = getUser(repoId, userId);
        if (user == null) {
            return resources;
        }


        List<Group> userGroups = getGroupsFromUser(repoId, userId);

        //first add permissions from groups as the ones specific to the user take priority so they overwrite these ones if the same ones apply

        for (Group group : userGroups) {
            Set<Permission<GroupId>> groupPermissions = group.getPermissions();
            for (Permission<GroupId> permission : groupPermissions) {
                if (antPathMatcher.isPattern(permission.getPath().path())) {
                    antPaths.put(permission.getPath().path(), permission.getPermission());
                } else {
                    fullPaths.put(permission.getPath().path(), permission.getPermission());
                }
            }
        }

        Set<Permission<UserId>> permissions = user.getPermissions();
        for (Permission<UserId> permission : permissions) {
            if (antPathMatcher.isPattern(permission.getPath().path())) {
                antPaths.put(permission.getPath().path(), permission.getPermission());
            } else {
                fullPaths.put(permission.getPath().path(), permission.getPermission());
            }
        }


        //todo:jmd seperate to method that only evaluated the paths not entire resources, then I can cache those without much memory overhead for storing them


        //todo:jmd first evaluate all specific ones those don't need to be iterated since those have a higher priority
        for (Resource resource : resources) {
            for (Map.Entry<String, PermissionType> entry : paths.entrySet()) {
                if (resource.resourcePath().startsWith(entry.getKey())) {
                    resource.setPermissionType(entry.getValue());
                }
            }
        }


        for ()


            //user specific permission takes priority over group permissions if both are present for the same resource,
            //in that case make Map with all paths and the permission type, then sort by length and iterate over them?
            return resources;
    }


    @Cacheable(cacheNames = "userPermissions", key = "{#userId,#repoId,#resourcePaths}")
    public List<Path> filterUsingPermissions(RepoId repoId, UserId userId, List<Path> resourcePaths) {
        Map<String, PermissionType> antPaths = new HashMap<>();
        Map<String, PermissionType> fullPaths = new HashMap<>();

        UserProfile user = getUser(repoId, userId);
        if (user == null) {
            return resources;
        }


        List<Group> userGroups = getGroupsFromUser(repoId, userId);

        //first add permissions from groups as the ones specific to the user take priority so they overwrite these ones if the same ones apply

        for (Group group : userGroups) {
            Set<Permission<GroupId>> groupPermissions = group.getPermissions();
            for (Permission<GroupId> permission : groupPermissions) {
                if (antPathMatcher.isPattern(permission.getPath().path())) {
                    antPaths.put(permission.getPath().path(), permission.getPermission());
                } else {
                    fullPaths.put(permission.getPath().path(), permission.getPermission());
                }
            }
        }

        Set<Permission<UserId>> permissions = user.getPermissions();
        for (Permission<UserId> permission : permissions) {
            if (antPathMatcher.isPattern(permission.getPath().path())) {
                antPaths.put(permission.getPath().path(), permission.getPermission());
            } else {
                fullPaths.put(permission.getPath().path(), permission.getPermission());
            }
        }


        //todo:jmd seperate to method that only evaluated the paths not entire resources, then I can cache those without much memory overhead for storing them


        //todo:jmd first evaluate all specific ones those don't need to be iterated since those have a higher priority
        for (Resource resource : resources) {
            for (Map.Entry<String, PermissionType> entry : paths.entrySet()) {
                if (resource.resourcePath().startsWith(entry.getKey())) {
                    resource.setPermissionType(entry.getValue());
                }
            }
        }


        for ()


            return resourcePaths;
    }
}
