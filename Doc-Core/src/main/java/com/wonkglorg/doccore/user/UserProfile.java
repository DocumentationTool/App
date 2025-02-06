package com.wonkglorg.doccore.user;

import com.wonkglorg.user.PermissionNode;

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
