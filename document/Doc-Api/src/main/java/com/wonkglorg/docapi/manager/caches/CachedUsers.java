package com.wonkglorg.docapi.manager.caches;

import com.wonkglorg.docapi.common.GroupId;
import com.wonkglorg.docapi.common.UserId;
import com.wonkglorg.docapi.manager.FileRepository;
import com.wonkglorg.docapi.permissions.Permission;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Stores all permissions assigned to each group, for quicker access
     */
    private final Map<GroupId, List<Permission<GroupId>>> cachedGroupPermissions = new HashMap<>();

    /**
     * Stores all permissions assigned to each individual user, for quicker access
     */
    private final Map<UserId, List<Permission<UserId>>> cachedUserPermissions = new HashMap<>();


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
        return cachedUserPermissions.get(id);
    }

    /**
     * @param groupId
     * @return
     */
    public List<Permission<GroupId>> getGroupPermissions(GroupId groupId) {
        return cachedGroupPermissions.get(groupId);
    }

    //todo:jmd merge with caches resources, also cache all paths the users has access to?
    // probably better than having to build up that path each time it changes?


}
