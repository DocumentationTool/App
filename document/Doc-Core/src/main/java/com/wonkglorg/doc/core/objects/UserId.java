package com.wonkglorg.doc.core.objects;

import java.util.function.Predicate;

/**
 * A Users unique identifier
 */
public record UserId(String id) implements Identifyable {
    public static UserId of(String id) {
        return new UserId(id);
    }


    /**
     * Filters all matching repos or lets all pass if id is null
     */
    public Predicate<UserId> filter() {
        return (UserId userId) -> id == null || userId.equals(this);
    }


    @Override
    public String toString() {
        return id;
    }
}
