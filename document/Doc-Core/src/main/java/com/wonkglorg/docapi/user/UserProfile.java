package com.wonkglorg.docapi.user;


import com.wonkglorg.docapi.permissions.PermissionNode;
import com.wonkglorg.docapi.permissions.Role;

import java.util.List;

public interface UserProfile {
    /**
     * Gets the users id
     */
    String getId();

    /**
     * The Username of the profile
     */
    String getUsername();

    /**
     * @return the users hashed password
     */
    String getPasswordHash();
    
    /**
     * The NodeMap of all permissions this user has
     */
    List<PermissionNode> permissions();
    
    /**
     * @return The roles this user has
     */
    List<Role> roles();
    
}
