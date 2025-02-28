package com.wonkglorg.doc.core.objects;

public record RoleId(String id) implements Identifyable {
    public static RoleId of(String id) {
        return new RoleId(id);
    }
}
