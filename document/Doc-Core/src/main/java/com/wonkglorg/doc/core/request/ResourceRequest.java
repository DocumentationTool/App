package com.wonkglorg.doc.core.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceRequest{
	/**
	 * The term to search text by
	 */
	public String searchTerm;
	/**
	 * The path to search by may be an ant path
	 */
	public String path;
	/**
	 * The repo to search in
	 */
	public String repoId;
	/**
	 * The user to search limit the search to
	 */
	public String userId;
	/**
	 * The tags to search by
	 */
	public List<String> whiteListTags = new ArrayList<>();
	/**
	 * The tags to exclude from the search
	 */
	public List<String> blacklistTags = new ArrayList<>();
	/**
	 * If the data of the resource should be returned
	 */
	public boolean withData = false;
	/**
	 * The limit of results to return
	 */
	public int returnLimit = 999999999;
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ResourceRequest that)){
			return false;
		}
		return withData == that.withData && returnLimit == that.returnLimit && Objects.equals(searchTerm, that.searchTerm) && Objects.equals(path,
				that.path) && Objects.equals(repoId, that.repoId) && Objects.equals(userId, that.userId) && Objects.equals(whiteListTags,
				that.whiteListTags) && Objects.equals(blacklistTags, that.blacklistTags);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(searchTerm, path, repoId, userId, whiteListTags, blacklistTags, withData, returnLimit);
	}
}
