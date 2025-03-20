package com.wonkglorg.doc.core.objects;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A Users unique identifier
 */
public class UserId implements Identifyable {
    /**
     * A special id that represents all users
     */
    public static final UserId ALL_USERS = new UserId("0000-0000-0000-0000-ALL_USERS");
    private final String id;

    private UserId(String id) {
        this.id = id;
    }

    public static UserId of(String id) {
        return id == null ? ALL_USERS : new UserId(id);
    }

    /**
     * Filters all matching repos or lets all pass if id is null
     */
    public Predicate<UserId> filter() {
        return (UserId userId) -> id == null || userId.equals(this);
    }

    /**
     * Checks if this id represents all repos
     */
    public boolean isAllUsers() {
        return this.equals(ALL_USERS);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserId userId)) {
            return false;
        }
        return Objects.equals(id, userId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String id() {
        return id;
    }
}
