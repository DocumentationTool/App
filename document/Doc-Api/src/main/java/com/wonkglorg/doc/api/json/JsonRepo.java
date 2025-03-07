package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.RepoProperty;

import java.util.ArrayList;
import java.util.List;

public class JsonRepo {
    public String id;
    public String path;
    public String dbName;
    private String dbStorage;
    public boolean isReadOnly;

    public JsonRepo(RepoProperty property) {
        id = property.getId().id();
        path = property.getPath() == null ? null : property.getPath().toString();
        dbName = property.getDbName();
        dbStorage = property.getDbStorage() == null ? null : property.getDbStorage().toString();
        isReadOnly = property.isReadOnly();
    }


    public static List<JsonRepo> from(List<RepoProperty> properties) {
        List<JsonRepo> repos = new ArrayList<>();
        for (RepoProperty prop : properties) {
            repos.add(new JsonRepo(prop));
        }
        return repos;
    }
}
