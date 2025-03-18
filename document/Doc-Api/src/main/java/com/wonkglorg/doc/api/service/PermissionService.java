package com.wonkglorg.doc.api.service;

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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionService {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final UserService userService;

    public PermissionService(@Lazy UserService userService) {
        this.userService = userService;
    }


    /**
     * Checks the access type of a user to a path
     *
     * @param repoId the repo id
     * @param userId the user id
     * @param path   the path
     * @return the permission type
     */
    public PermissionType accessType(RepoId repoId, UserId userId, String path) throws InvalidRepoException, InvalidUserException {
        UserProfile user = userService.getUser(repoId, userId);
        List<Group> groupsFromUser = userService.getGroupsFromUser(repoId, userId);

        Set<Permission<UserId>> userPermissions = user.getPermissions();
        Set<Permission<GroupId>> groupPermissions = new HashSet<>();
        if (groupsFromUser != null) {
            for (Group group : groupsFromUser) {
                groupPermissions.addAll(group.getPermissions());
            }
        }

//todo;jmd fix
        //Permission.filterPathsWithPermissions(userPermissions, groupPermissions, path);

        Map<String, PermissionType> fullPathsGroup = new HashMap<>();
        TreeMap<String, PermissionType> antPathsGroup = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));

        Map<String, PermissionType> fullPathsUser = new HashMap<>();
        TreeMap<String, PermissionType> antPathsUser = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));
        //no permissions set in groups or user
        if (groupPermissions.isEmpty() && userPermissions.isEmpty()) {
            return PermissionType.DENY;
        }

        //collect paths
        /*
        if (!groupPermissions.isEmpty()) {
            for (var permission : groupPermissions) {
                storePermission(permission, antPathsGroup, fullPathsGroup);
            }
        }

        if (!userPermissions.isEmpty()) {
            for (var permission : userPermissions) {
                storePermission(permission, antPathsUser, fullPathsUser);
            }
        }

         */
        //todo:jmd fix
        return Permission.permissionForPath(path, fullPathsUser, antPathsUser, fullPathsGroup, antPathsGroup);
    }


    /**
     * Checks the access type of a user to a path
     *
     * @param repoId the repo id
     * @param userId the user id
     * @param path   the path
     * @return the permission type
     */
    public PermissionType accessType(RepoId repoId, UserId userId, Path path) throws InvalidRepoException, InvalidUserException {
        return accessType(repoId, userId, path.toString());
    }


    /**
     * Filters a list of resources based on the permissions of a user if non is given return all resources with permission access Edit
     *
     * @param userPermissions  the permissions of the user (can be null)
     * @param groupPermissions the permissions of the groups the user is in (can be null)
     * @param resources        the resources to filter
     * @return the filtered resources
     */
    public List<Resource> filterResources(Set<Permission<UserId>> userPermissions,
                                          Set<Permission<GroupId>> groupPermissions,
                                          List<Resource> resources) {

        List<Path> resourcePaths = resources.stream().map(Resource::resourcePath).collect(Collectors.toList());

        Map<Path, PermissionType> filteredResources = new HashMap<>();//filterWithPermissions(userPermissions, groupPermissions, resourcePaths);

        //noinspection SimplifyStreamApiCallChains not a good idea as peak can be skipped by the compiler
        return resources.stream().filter(resource -> filteredResources.containsKey(resource.resourcePath())).map(resource -> {
            PermissionType permission = filteredResources.get(resource.resourcePath());
            resource.setPermissionType(permission);
            return resource;
        }).collect(Collectors.toList());
    }

    

    /**
     * Filters a list of resources based on the permissions of a user if non is given return all resources with permission access Edit
     *
     * @param repoId    the repo id
     * @param userId    the user id
     * @param resources the resources to filter
     * @return the filtered resources
     * @throws InvalidRepoException if the repo is invalid
     * @throws InvalidUserException if the user is invalid
     */
    public List<Resource> filterResources(RepoId repoId, UserId userId, List<Resource> resources) throws InvalidRepoException, InvalidUserException {
        UserProfile user = userService.getUser(repoId, userId);
        List<Group> groupsFromUser = userService.getGroupsFromUser(repoId, userId);
        Set<Permission<UserId>> permissions = user.getPermissions();
        Set<Permission<GroupId>> groupPermissions = new HashSet<>();
        for (Group group : groupsFromUser) {
            //on conflict use the one that allows or denies?
            groupPermissions.addAll(group.getPermissions());
        }
        return filterResources(permissions, groupPermissions, resources);
    }

    //todo:jmd move to core, as thats not service specific

}
