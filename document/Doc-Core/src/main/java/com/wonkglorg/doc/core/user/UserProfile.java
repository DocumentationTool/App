package com.wonkglorg.doc.core.user;

import com.google.gson.Gson;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The users profile
 */
public class UserProfile {
    private final Gson gson = new Gson();
    private final UserId id;
    private final String passwordHash;
    private final Set<Permission<UserId>> permissionNodes;
    private final Set<Role> roles;
    private final Set<GroupId> groups = new HashSet<>();

    public UserProfile(UserId id, String passwordHash, Set<Permission<UserId>> permissionNodes, Set<Role> roles) {
        this.id = id;
        this.permissionNodes = permissionNodes;
        this.roles = roles;
        this.passwordHash = passwordHash;
    }

    //todo:jmd implement
    public Collection<Resource> getAllowedResources(Collection<Resource> resources) {
        return resources;
    }

    public UserId getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<Permission<UserId>> getPermissions() {
        return permissionNodes;
    }

    public Set<GroupId> getGroups() {
        return groups;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
