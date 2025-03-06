package com.wonkglorg.doc.api.json;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.wonkglorg.doc.core.permissions.Permission;

import java.util.HashMap;
import java.util.Map;

/**
 * A Json view of a permission
 */
public class JsonPermissions {

    public final Map<String, JsonPermission> permissions = new HashMap<>();




    public class JsonPermission {
        public final String id;
        public final boolean isGroup;
        public final boolean isUser;
        public final String path;


        public JsonPermission(Permission<?> permission) {
            id = permission.getId();
            isGroup = permission.isGroup();
            isUser = permission.isUser();
            path = permission.getPath().path();
        }
    }
}
