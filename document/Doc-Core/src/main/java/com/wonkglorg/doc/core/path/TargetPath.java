package com.wonkglorg.doc.core.path;

import java.nio.file.Path;

/**
 * Represents a path to a target, can be either a normal path or an ant path (a path with wildcards)
 */
public class TargetPath {
    private Path path;
    private AntPath antPath;


    public TargetPath(String path) {
        if (AntPath.isAntPath(path)) {
            this.antPath = new AntPath(path);
            this.path = null;
        } else {
            this.antPath = null;
            this.path = normalizePath(Path.of(path));
        }
    }

    public static TargetPath of(String path) {
        return new TargetPath(path);
    }

    public static TargetPath of(Path path) {
        return new TargetPath(path.toString());
    }


    /**
     * If the path is an ant path
     *
     * @return true if the path is an ant path
     */
    public boolean isAntPath() {
        return antPath != null;
    }

    /**
     * Normalizes the path to the correct one to be specified in the database
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    public static Path normalizePath(Path path) {
        return Path.of(normalizePath(path.toString()));
    }

    public static String normalizePath(String path) {
        return path.replace("/", "\\").replace("\\\\", "\\");
    }


    public Path getPath() {
        return path;
    }

    public AntPath getAntPath() {
        return antPath;
    }

    /**
     * Returns the path as a string the string method returns either the {@link Path} or {@link AntPath} depending on which one is set
     */
    @Override
    public String toString() {
        return antPath == null ? path.toString() : antPath.toString();
    }
}
