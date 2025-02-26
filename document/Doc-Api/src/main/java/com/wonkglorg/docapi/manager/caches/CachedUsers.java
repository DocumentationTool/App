package com.wonkglorg.docapi.manager.caches;

import com.wonkglorg.docapi.common.GroupId;
import com.wonkglorg.docapi.common.UserId;
import com.wonkglorg.docapi.manager.FileRepository;
import com.wonkglorg.docapi.permissions.Permission;
import com.wonkglorg.docapi.permissions.PermissionType;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CachedUsers extends CacheableResource {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    //these 2 are seperated to make querying for either faster
    /**
     * Relates groups to their matching users
     */
    private final Map<GroupId, List<UserId>> groupUserCache = new HashMap<>();
    /**
     * Relates users to their matching groups
     */
    private final Map<UserId, List<GroupId>> userGroupCache = new HashMap<>();

    private final Map<UserId, Map<PermissionType, Permission<UserId>>> cachedUserPermissions = new HashMap<>();
    private final Map<GroupId, Map<PermissionType, Permission<GroupId>>> cachedGroupPermissions = new HashMap<>();


    public CachedUsers(FileRepository repository) {
        super(repository);
    }

    @Override
    public void rebuild() {

    }


    /**
     * Gets all users from a specified group
     *
     * @param groupId the group to look for
     * @return the users in the group
     */
    public List<UserId> getUsers(GroupId groupId) {
        return groupUserCache.get(groupId);
    }


    /**
     * Get all groups a user is in
     *
     * @param id the user id
     * @return all {@link GroupId}the users belongs to
     */
    public List<GroupId> getGroups(UserId id) {
        return userGroupCache.get(id);
    }


    /**
     * Gets the permissions defined for a user
     *
     * @param id
     * @return
     */
    public List<Permission<UserId>> getUserPermissions(UserId id) {
        return cachedUserPermissions.get(id).values().stream().toList();
    }

    /**
     * @param groupId
     * @return
     */
    public List<Permission<GroupId>> getGroupPermissions(GroupId groupId) {
        return cachedGroupPermissions.get(groupId).values().stream().toList();
    }

    public List<Permission<GroupId>> getGroupPermissions(GroupId groupId, PermissionType... types) {
        return cachedGroupPermissions.get(groupId).entrySet().stream().filter(entry -> {
            for (var type : types) {
                if (type.equals(entry.getKey())) {
                    return true;
                }
            }
            return false;
        }).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<Permission<UserId>> getUserPermissions(GroupId groupId, PermissionType... types) {
        return cachedUserPermissions.get(groupId).entrySet().stream().filter(entry -> {
            for (var type : types) {
                if (type.equals(entry.getKey())) {
                    return true;
                }
            }
            return false;
        }).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    //todo:jmd merge with caches resources, also cache all paths the users has access to?
    // probably better than having to build up that path each time it changes?


    //todo:jmd how do I correctly match those 2? to get all the files the user can access and in what form?



}
