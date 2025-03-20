package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.user.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class JsonUser {
    public final String userId;
    public final List<JsonRole> roles = new ArrayList<>();
    public final List<JsonPermission> permissions = new ArrayList<>();
    public final List<String> groups = new ArrayList<>();


    public JsonUser(UserProfile user) {
        this.userId = user.getId().id();
        user.getRoles().forEach(r -> roles.add(new JsonRole(r.roleID().id(), r.roleName())));
        user.getPermissions().values().forEach(p -> permissions.add(new JsonPermission(p)));
        user.getGroups().forEach(g -> groups.add(g.id()));

    }
}
