package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.permissions.Permission;

public class JsonPermission {
    public final String id;
    public final boolean isGroup;
    public final boolean isUser;
    public final String path;


    public JsonPermission(Permission<?> permission) {
        id = permission.getId();
        isGroup = permission.isGroup();
        isUser = permission.isUser();
        path = permission.getPath().toString();
    }
}

