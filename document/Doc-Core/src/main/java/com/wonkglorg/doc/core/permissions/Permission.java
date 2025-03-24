package com.wonkglorg.doc.core.permissions;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Identifyable;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a permission
 *
 * @param <T>
 */
public class Permission<T extends Identifyable> {


    /**
     * The AntPathMatcher
     */
    private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * The User this permission is for
     */
    private final T id;

    /**
     * The permissions associated with this Node
     */
    private PermissionType permission;

    /**
     * The path this resource permission is for
     */
    private TargetPath path;

    /**
     * The repo this permission belongs to
     */
    private RepoId repoId;

    public Permission(T id, PermissionType permission, TargetPath path, RepoId repoId) {
        this.id = id;
        this.permission = permission;
        this.path = path;
        this.repoId = repoId;
    }


    public T id() {
        return id;
    }


    public String getId() {
        return id.id();
    }

    public boolean isGroup() {
        return id instanceof GroupId;
    }

    public boolean isUser() {
        return id instanceof UserId;
    }

    public void setPermission(PermissionType permission) {
        this.permission = permission;
    }


    public PermissionType getPermission() {
        return permission;
    }

    public void setPath(TargetPath path) {
        this.path = path;
    }

    public void setRepoId(RepoId repoId) {
        this.repoId = repoId;
    }

    public RepoId getRepoId() {
        return repoId;
    }

    public TargetPath getPath() {
        return path;
    }


    /**
     * Checks the access type of a user to a path
     *
     * @param path
     * @param fullPathsUser
     * @param antPathsUser
     * @param fullPathsGroup
     * @param antPathsGroup
     * @return
     */
    private static PermissionType permissionForPath(Path path, Map<String, PermissionType> fullPathsUser, TreeMap<String, PermissionType> antPathsUser, Map<String, PermissionType> fullPathsGroup, TreeMap<String, PermissionType> antPathsGroup) {
        return permissionForPath(path.toString(), fullPathsUser, antPathsUser, fullPathsGroup, antPathsGroup);
    }


    /**
     * Checks the access type of a user to a path
     *
     * @param path the path
     * @return the permission type
     */
    public static PermissionType permissionForPath(String path, Map<String, PermissionType> fullPathsUser, TreeMap<String, PermissionType> antPathsUser, Map<String, PermissionType> fullPathsGroup, TreeMap<String, PermissionType> antPathsGroup) {
        path = TargetPath.normalizePath(path);
        if (fullPathsUser.containsKey(path)) {
            return fullPathsUser.get(path);
        } else if (fullPathsGroup.containsKey(path)) {
            return fullPathsGroup.get(path);
        }

        // 2. Check ant paths (user-specific first, then group)
        for (var entry : antPathsUser.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), path)) {
                return entry.getValue();
            }
        }


        for (var entry : antPathsGroup.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), path)) {
                return entry.getValue();
            }
        }

        return PermissionType.DENY;
    }

    /**
     * Filters a list of resources based on the permissions of a user if non is given return all resources with permission access Edit
     *
     * @param userPermissions  the permissions of the user (can be null)
     * @param groupPermissions the permissions of the groups the user is in (can be null)
     * @param resourcePaths    the paths of the resources
     * @return a map of the resources and their permissions
     */
    public static Map<Path, PermissionType> filterPathsWithPermissions(Set<Permission<UserId>> userPermissions,
                                                                       Set<Permission<GroupId>> groupPermissions, List<Path> resourcePaths) {
        Map<String, PermissionType> fullPathsGroup = new HashMap<>();
        TreeMap<String, PermissionType> antPathsGroup = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));

        Map<String, PermissionType> fullPathsUser = new HashMap<>();
        TreeMap<String, PermissionType> antPathsUser = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));

        if (userPermissions == null && groupPermissions == null) {
            return resourcePaths.stream().map(TargetPath::normalizePath).collect(Collectors.toMap(path -> path, path -> PermissionType.DENY));
        }
        // Then collect user permissions (ensuring they overwrite group permissions)
        if (userPermissions != null) {
            for (var permission : userPermissions) {
                storePermission(permission, antPathsUser, fullPathsUser);
            }
        }
        // First collect group permissions
        if (groupPermissions != null) {
            for (var permission : groupPermissions) {
                storePermission(permission, antPathsGroup, fullPathsGroup);
            }
        }


        Map<Path, PermissionType> result = new HashMap<>();

        //apply perms
        for (Path resourcePath : resourcePaths) {
            PermissionType appliedPermission = permissionForPath(resourcePath, fullPathsUser, antPathsUser, fullPathsGroup, antPathsGroup);
            if (appliedPermission != null) {
                result.putIfAbsent(resourcePath, appliedPermission);
            }
        }

        return result;
    }

    /**
     * Checks the access type of a user to a path
     *
     * @param userPermissions  the permissions of the user (can be null)
     * @param groupPermissions the permissions of the groups the user is in (can be null)
     * @param path             the path
     * @return the permission type
     */
    public static PermissionType accessType(Set<Permission<UserId>> userPermissions,
                                            Set<Permission<GroupId>> groupPermissions,
                                            Path path) {
        Map<String, PermissionType> fullPathsGroup = new HashMap<>();
        TreeMap<String, PermissionType> antPathsGroup = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));

        Map<String, PermissionType> fullPathsUser = new HashMap<>();
        TreeMap<String, PermissionType> antPathsUser = new TreeMap<>((a, b) -> Integer.compare(b.length(), a.length()));

        if (groupPermissions.isEmpty() && userPermissions.isEmpty()) {
            return PermissionType.DENY;
        }

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
        return permissionForPath(path, fullPathsUser, antPathsUser, fullPathsGroup, antPathsGroup);
    }


    private static void storePermission(Permission<?> permission, TreeMap<String, PermissionType> antPaths, Map<String, PermissionType> fullPaths) {
        String path = permission.getPath().toString();
        if (antPathMatcher.isPattern(path)) {
            antPaths.put(path, permission.getPermission());
        } else {
            fullPaths.put(path, permission.getPermission());
        }
    }

}
