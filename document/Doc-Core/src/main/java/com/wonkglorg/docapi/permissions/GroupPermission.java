package com.wonkglorg.docapi.permissions;

import com.wonkglorg.docapi.common.GroupId;
import com.wonkglorg.docapi.common.ResourcePath;

/**
 * Represents a Group Permission on either a resource or ant path
 */
public class GroupPermission implements PermissionNode {

    private GroupId groupId;

    private Permission permission;

    private ResourcePath path;

    public GroupPermission(GroupId groupId, ResourcePath path, Permission permission) {
        this.groupId = groupId;
        this.permission = permission;
        this.path = path;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    @Override
    public ResourcePath getPath() {
        return path;
    }

    @Override
    public Permission getPermission() {
        return permission;
    }


}
