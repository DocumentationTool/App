package com.wonkglorg.doc.core.objects;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a resource path, can be both a full path or ant path ending in a "*" wildcard
 *
 * @param path
 */
public record ResourcePath(String path) {

    public static ResourcePath of(String path) {
        return new ResourcePath(normalizePath(path));
    }

    public static ResourcePath of(Path path) {

        return new ResourcePath(normalizePath(path));
    }

    /**
     * Normalizes the path to the correct one to be specified in the database
     * @param path the path to normalize
     * @return the normalized path
     */
    public static Path normalizePath(Path path) {
        return Path.of(normalizePath(path.toString()));
    }

    public static String normalizePath(String path) {
        return path.replace("\\", "/").replace("//", "/");
    }

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
