package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class JsonUser {
    public final String userId;
    public final List<Role> roles = new ArrayList<>();
    public final List<JsonPermission> permissions = new ArrayList<>();
    public final List<String> groups = new ArrayList<>();


    public JsonUser(UserProfile user) {
        this.userId = user.getId().id();
        roles.addAll(user.getRoles());
        user.getPermissions().values().forEach(p -> permissions.add(new JsonPermission(p)));
        user.getGroups().forEach(g -> groups.add(g.id()));

    }
}
