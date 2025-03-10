package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.objects.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonFileTree{
	private String name;
	private Map<String, JsonFileTree> children = new HashMap<>();
	private List<JsonResource> resources = new ArrayList<>();
	
	public JsonFileTree(String name) {
		this.name = name;
	}
	
	// Adds a subdirectory or retrieves an existing one
	public JsonFileTree add(String childName) {
		return children.computeIfAbsent(childName, JsonFileTree::new);
	}
	
	// Adds a resource (file) to this directory
	public void addResource(Resource resource) {
		resources.add(JsonResource.of(resource));
	}
	
	public Map<String, JsonFileTree> getChildren() {
		return children;
	}
	
	public List<JsonResource> getResources() {
		return resources;
	}
}
