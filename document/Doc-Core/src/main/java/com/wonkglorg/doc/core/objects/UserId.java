package com.wonkglorg.doc.core.objects;

/**
 * A Users unique identifier
 */
public record UserId(String id) implements Identifyable {
    public static UserId of(String id) {
        return new UserId(id);
    }
}
