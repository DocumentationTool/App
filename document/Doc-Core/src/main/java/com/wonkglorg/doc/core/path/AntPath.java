package com.wonkglorg.doc.core.path;

import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.Objects;

import static com.wonkglorg.doc.core.path.TargetPath.normalizePath;

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

        this.path = normalizePath(path);
    }

    /**
     * Checks if the path is an ant path
     * @param path the path to check
     * @return true if the path is an ant path
     */
    public static boolean isAntPath(String path) {
        return matcher.isPattern(path);
    }


    /**
     * Checks if the path matches the ant path
     *
     * @param path the path to check
     * @return true if the path matches the ant path
     */
    public boolean matches(String path) {
        return matcher.match(this.path, normalizePath(path));
    }

    /**
     * Checks if the path matches the ant path
     *
     * @param path the path to check
     * @return true if the path matches the ant path
     */
    public boolean matches(Path path) {
        return matches(path.toString());
    }


    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AntPath antPath = (AntPath) o;
        return Objects.equals(path, antPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
