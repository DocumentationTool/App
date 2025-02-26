package com.wonkglorg.docapi.user;


import com.google.gson.Gson;
import com.wonkglorg.docapi.permissions.Role;

import java.util.List;

public interface UserProfile {
    Gson gson = new Gson();

    /**
     * Gets the users id
     */
    String getId();
    /**
     * @return The users hashed password from the database
     */
    String getPasswordHash();

    /**
     * The NodeMap of all permissions this user has
     */
    List<PermissionNode> getPermissions();

    /**
     * @return The roles this user has
     */
    List<Role> getRoles();

    default String toJson() {
        return gson.toJson(this);
    }

}
