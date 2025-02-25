package com.wonkglorg.docapi.permissions;

import com.wonkglorg.docapi.common.ResourcePath;
import com.wonkglorg.docapi.common.UserId;

public class UserPermission implements PermissionNode {
    private UserId userId;

    private Permission permission;

    private ResourcePath path;


    public UserPermission(UserId userId, ResourcePath path, Permission permission) {
        this.userId = userId;
        this.permission = permission;
        this.path = path;
    }

    @Override
    public ResourcePath getPath() {
        return path;
    }

    @Override
    public Permission getPermission() {
        return permission;
    }

    public UserId getUserId() {
        return userId;
    }
}
