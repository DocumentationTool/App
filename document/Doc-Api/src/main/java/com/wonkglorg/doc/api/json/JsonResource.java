package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.objects.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Json representation of a resource
 */
public class JsonResource {
    public String path;
    public String repoId;
    public String createdBy;
    public String createdAt;
    public String category;
    public String lastModifiedBy;
    public String lastModifiedAt;
    public boolean isEditable;
    public String content;

    private JsonResource(Resource resource) {
        path = resource.resourcePath().toString();
        repoId = resource.repoId().toString();
        createdBy = resource.createdBy();
        createdAt = resource.getCreatedAt();
        category = resource.category();
        lastModifiedBy = resource.modifiedBy();
        lastModifiedAt = resource.getModifiedAt();
        content = resource.data();
        isEditable = resource.isEditable();
    }

    public static JsonResource of(Resource resource) {
        return new JsonResource(resource);
    }


    public static List<JsonResource> of(List<Resource> resources) {
        return resources.stream().map(JsonResource::new).collect(Collectors.toList());
    }
}
