package com.wonkglorg.doc.core.objects;

/**
 * A groups unique identifier
 */
public record GroupId(String id) implements Identifyable{
    public static GroupId of(String id) {
        return new GroupId(id);
    }
}
