package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.permissions.PermissionType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Json representation of a resource
 */
public class JsonResource{
	public String path;
	public String repoId;
	public String createdBy;
	public String createdAt;
	public String category;
	public Set<String> tags = new HashSet<>();
	public String lastModifiedBy;
	public String lastModifiedAt;
	public PermissionType permissionType;
	public String data;
	
	private JsonResource(Resource resource) {
		path = resource.resourcePath().toString();
		repoId = resource.repoId().toString();
		createdBy = resource.createdBy();
		createdAt = resource.getCreatedAt();
		category = resource.category();
		lastModifiedBy = resource.modifiedBy();
		lastModifiedAt = resource.getModifiedAt();
		data = resource.data();
		permissionType = resource.getPermissionType();
		for(var tag : resource.getResourceTags()){
			tags.add(tag.id());
		}
	}
	
	public static JsonResource of(Resource resource) {
		return new JsonResource(resource);
	}
	
	public static List<JsonResource> of(List<Resource> resources) {
		return resources.stream().map(JsonResource::new).collect(Collectors.toList());
	}
}
