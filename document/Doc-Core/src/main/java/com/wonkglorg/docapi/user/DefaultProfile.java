package com.wonkglorg.docapi.user;


import com.wonkglorg.docapi.permissions.PermissionNode;
import com.wonkglorg.docapi.permissions.Role;

import java.util.ArrayList;
import java.util.List;

public class DefaultProfile implements UserProfile {
    private final String id;
    private final String passwordHash;
    private final List<PermissionNode> permissionNodes;
    private final List<Role> roles;

    public DefaultProfile(String id, String passwordHash) {
        this.id = id;
        this.permissionNodes = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.passwordHash = passwordHash;
    }

    public DefaultProfile(String id,String passwordHash, List<PermissionNode> permissionNodes, List<Role> roles) {
        this.id = id;
        this.permissionNodes = permissionNodes;
        this.roles = roles;
        this.passwordHash = passwordHash;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public List<PermissionNode> getPermissions() {
        return permissionNodes;
    }

    @Override
    public List<Role> getRoles() {
        return roles;
    }
}
