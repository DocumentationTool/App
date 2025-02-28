package com.wonkglorg.doc.core.objects;

/**
 * A repos unique identifier
 */
public record RepoId(String id) implements Identifyable {
    public static RepoId of(String id) {
        return new RepoId(id);
    }
}
