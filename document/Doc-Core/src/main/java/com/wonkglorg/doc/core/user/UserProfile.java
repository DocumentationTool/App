package com.wonkglorg.doc.core.user;

import com.google.gson.Gson;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The users profile
 */
public class UserProfile {
    private final Gson gson = new Gson();
    private final UserId id;
    private final String passwordHash;
    private final Set<Permission<UserId>> permissionNodes;
    private final Set<Role> roles = new HashSet<>();
    private final Set<GroupId> groups = new HashSet<>();

    public UserProfile(UserId id, String password, Set<Permission<UserId>> permissionNodes, Set<Role> roles, Set<GroupId> groups) {
        this.id = id;
        this.permissionNodes = permissionNodes;
        this.roles.addAll(roles);
        //todo:jmd hash it otherwise people gonna be mad
        this.passwordHash = password;
        this.groups.addAll(groups);
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
