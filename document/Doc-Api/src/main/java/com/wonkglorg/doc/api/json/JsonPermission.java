package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.permissions.Permission;

public class JsonPermission {
    public final String id;
    public final String path;
    public final String type;


    public JsonPermission(Permission<?> permission) {
        id = permission.getId();
        type = permission.getPermission().toString();
        path = permission.getPath().toString();
    }
}

