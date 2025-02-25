package com.wonkglorg.docapi.permissions;


import com.wonkglorg.docapi.common.ResourcePath;

/**
 * Defines a Permission node this could be a file, or directory any
 * permission defined on a directory automatically applies to all
 * children but each child can separately define other permissions that overwrite the previous defined permission
 */
public interface PermissionNode {
    /**
     * The path this permission points to
     */
    ResourcePath getPath();


    /**
     * The permission this node has
     */
    Permission getPermission();


}
