package com.wonkglorg.doc.core.objects;

import java.util.function.Predicate;

/**
 * A repos unique identifier
 */
public record RepoId(String id) implements Identifyable {
    public static RepoId of(String id) {
        return new RepoId(id);
    }

    /**
     * Filters all matching repos or lets all pass if id is null
     */
    public Predicate<RepoId> filter() {
        return (RepoId repoId) -> id == null || repoId.equals(this);
    }

    @Override
    public String toString() {
        return id;
    }
}
