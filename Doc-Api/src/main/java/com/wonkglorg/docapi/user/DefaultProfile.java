package com.wonkglorg.docapi.user;

import com.wonkglorg.docapi.permissions.PermissionNode;

import java.util.List;

public class DefaultProfile implements UserProfile {

    private final String userName;
    private final List<PermissionNode> permissionNodes;

    public DefaultProfile(String userName, List<PermissionNode> permissionNodes) {
        this.userName = userName;
        this.permissionNodes = permissionNodes;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public List<PermissionNode> permissions() {
        return permissionNodes;
    }
}
