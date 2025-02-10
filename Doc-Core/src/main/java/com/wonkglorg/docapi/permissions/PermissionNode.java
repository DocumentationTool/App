package com.wonkglorg.docapi.permissions;


import java.util.List;

/**
 * Defines a Permission node this could be a file, or directory any
 * permission defined on a directory automatically applies to all
 * children but each child can separately define other permissions that overwrite the previous defined permission
 */
public interface PermissionNode {
    /**
     * The Name of the Node
     */
    String getNodeName();

    /**
     * All Permissions this user has for the specified node
     */
    List<Permission> getPermissions();
}
