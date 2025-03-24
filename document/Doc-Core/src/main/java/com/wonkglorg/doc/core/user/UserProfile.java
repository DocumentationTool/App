package com.wonkglorg.doc.core.user;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The users profile
 */
public class UserProfile {
    private final UserId id;
    private final String passwordHash;
    private final Set<GroupId> groups;

    public UserProfile(UserId id, String password, Map<String, Set<GroupId> groups) {
        this.id = id;
        this.passwordHash = password;
        this.groups = new HashSet<>(groups);
    }

    public List<Resource> getAllowedResources(List<Resource> resources) {
        return resources;
    }

    public UserId getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Check if the password hash matches the given password
     *
     * @param password the password to check
     * @return true if the password hash matches the given password
     */
    public boolean hashMatches(String password) {
        return passwordHash.equals(password);
    }

    public Map<String, Permission<UserId>> getPermissions() {
        return permissions;
    }

    public Set<Permission<UserId>> getPermissionsAsSet() {
        return new HashSet<>(permissions.values());
    }

    public Set<GroupId> getGroups() {
        return groups;
    }

    public Set<Role> getRoles() {
        return roles;
    }

}
