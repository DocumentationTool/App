package com.wonkglorg.docapi.manager.caches;

import com.wonkglorg.docapi.manager.FileRepository;

public abstract class CacheableResource {
    /**
     * The backing repository this cached resource accesses
     */
    protected final FileRepository repository;

    protected CacheableResource(FileRepository repository) {
        this.repository = repository;
    }

    /**
     * Rebuilds the cache from the backing source
     */
    public abstract void rebuild();
}
