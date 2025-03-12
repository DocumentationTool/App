package com.wonkglorg.doc.api.json;

public class JsonUser {


    public final String userId;
    public final JsonRoles roles;
    public final JsonPermissions permissions;

    public JsonUser(String userId, JsonRoles roles, JsonPermissions permissions) {
        this.userId = userId;
        this.roles = roles;
        this.permissions = permissions;
    }


}
