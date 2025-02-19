package com.wonkglorg.docapi.user;


import com.wonkglorg.docapi.permissions.PermissionNode;
import com.wonkglorg.docapi.permissions.Role;

import java.util.ArrayList;
import java.util.List;

public class DefaultProfile implements UserProfile {
    private final String id;
    private final List<PermissionNode> permissionNodes;
    private final List<Role> roles;

    public DefaultProfile(String id) {
        this.id = id;
        this.permissionNodes = new ArrayList<>();
        this.roles = new ArrayList<>();
    }

    public DefaultProfile(String id, List<PermissionNode> permissionNodes, List<Role> roles) {
        this.id = id;
        this.permissionNodes = permissionNodes;
        this.roles = roles;
    }

    public static DefaultProfile createDefault() {
        return new DefaultProfile("test_p10209", List.of(), List.of());
    }

    @Override
    public String getId() {
        return id;
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
