package com.wonkglorg.docapi.user;


import com.wonkglorg.docapi.permissions.PermissionNode;

import java.util.List;

public interface UserProfile {
    /**
     * The Username of the profile
     */
    String getUsername();

    /**
     * The NodeMap of all permissions this user has
     */
    List<PermissionNode> permissions();

}
