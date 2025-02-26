package com.wonkglorg.docapi.manager.caches;

import com.wonkglorg.docapi.common.Resource;
import com.wonkglorg.docapi.common.ResourcePath;
import com.wonkglorg.docapi.manager.FileRepository;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedResources extends CacheableResource {

    /**
     * Keeps track of all cached resources for quick access in each repo
     */
    private final Map<Path, Resource> cachedResources = new HashMap<>();

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public CachedResources(FileRepository repository) {
        super(repository);
    }


    @Override
    public void rebuild() {
        cachedResources.clear();
        List<Resource> resources = repository.getDataDB().getResources();
        resources.forEach(r -> cachedResources.put(r.resourcePath(), r));
    }

    /**
     * Returns a resource or a List of resources depending if the {@link ResourcePath relates to an antpath or a full qualified path}
     * @param resourcePath the path of an element or a directory / antpath
     * @return a list of matching resources or an empty list
     */
    public List<Resource> getFromPath(ResourcePath resourcePath) {
        String path = resourcePath.getPath().orElseThrow(() -> new IllegalArgumentException("Path cannot be null: " + resourcePath));
        if (antPathMatcher.isPattern(path)) {
            List<Resource> resources = new ArrayList<>();
            for (var entry : cachedResources.entrySet()) {
                if (antPathMatcher.match(entry.getKey().toString(), path)) {
                    resources.add(entry.getValue());
                }
            }
            return resources;
        } else {
            return List.of(cachedResources.get(Path.of(path)));
        }
    }
}
