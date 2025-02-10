package com.wonkglorg.docapi.user;


import com.wonkglorg.docapi.permissions.PermissionNode;

import java.util.ArrayList;
import java.util.List;

public class DefaultProfile implements UserProfile {

    private final String userName;
    private final List<PermissionNode> permissionNodes;

    public DefaultProfile(String userName) {
        this.userName = userName;
        this.permissionNodes = new ArrayList<>();
    }

    public DefaultProfile(String userName, List<PermissionNode> permissionNodes) {
        this.userName = userName;
        this.permissionNodes = permissionNodes;
    }

    public static DefaultProfile createDefault() {
        return new DefaultProfile("TestProfile", List.of());
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPasswordHash() {
        return "";
    }

    @Override
    public List<PermissionNode> permissions() {
        return permissionNodes;
    }

    @Override
    public String toString() {
        return "DefaultProfile{" +
                "userName='" + userName + '\'' +
                ", permissionNodes=" + permissionNodes +
                '}';
    }
}
