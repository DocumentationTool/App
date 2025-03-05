package com.wonkglorg.doc.core.objects;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a resource path, can be both a full path or ant path ending in a "*" wildcard
 *
 * @param path
 */
public record ResourcePath(String path) {

    public ResourcePath(Path path) {
        this(path.toString());
    }

    /**
     * @return an optional with the value or null if no value is present
     */
    public Optional<String> getPath() {
        return Optional.ofNullable(path);
    }
}
