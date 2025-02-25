package com.wonkglorg.docapi.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Represents a resource path, can be both a full path or ant path ending in a "*" wildcard
 * @param path
 */
public record ResourcePath(String path) {


    public ResourcePath(Path path) {
        this(path.toString());
    }

    /**
     * Weather or not the specified path is an ant path (ending in a *) ant paths cannot be converted to a path using {@link #toPath()}
     */
    public boolean isAntPath() {
        return path.endsWith("*");
    }


    /**
     * @return the {@link Path} representation of the specified path or an empty optional if the path is an ant path
     */
    public Optional<Path> toPath(){
        if(isAntPath()){
            return Optional.empty();
        }

        return Optional.of(Paths.get(path));
    }

    /**
     * @return the exact string definition for the specified path
     */
    public String getStringPath(){
        return path;
    }
}
