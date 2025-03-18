package com.wonkglorg.doc.core.objects;

import org.springframework.util.AntPathMatcher;

/**
 * Represents an antpath
 */
public class AntPath {
    private final String path;

    private static final AntPathMatcher matcher = new AntPathMatcher();

    public AntPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (!matcher.isPattern(path)) {
            throw new IllegalArgumentException("Path is not a valid ant path");
        }

        this.path = path.replace("/", "\\").replace("\\\\", "\\");
    }


    /**
     * Checks if the path matches the ant path
     *
     * @param path the path to check
     * @return true if the path matches the ant path
     */
    public boolean matches(String path) {
        path = path.replace("/", "\\").replace("\\\\", "\\");
        return matcher.match(this.path, path);
    }


    public String getPath() {
        return path;
    }


    @Override
    public String toString() {
        return path;
    }
}
