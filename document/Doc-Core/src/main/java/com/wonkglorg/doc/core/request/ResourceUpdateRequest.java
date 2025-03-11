package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.Tag;

import java.util.List;

public class ResourceUpdateRequest{
	
	/**
	 * The id of the repository
	 */
	public String repoId;
	/**
	 * The path to the resource
	 */
	public String path;
	/**
	 * The user to update the resource as
	 */
	public String userId;
	
	/**
	 * The tags to add to the resource
	 */
	public List<String> tagsToAdd;
	/**
	 * The tags to remove from the resource
	 */
	public List<String> tagsToRemove;
	/**
	 * The tags to set on the resource removes any previous tags
	 */
	public List<String> tagsToSet;
	
	/**
	 * The category to set on the resource
	 */
	public String category;
	
	/**
	 * The data to set on the resource
	 */
	public String data;
	
	public boolean treatNullsAsValues = false;
	
}
