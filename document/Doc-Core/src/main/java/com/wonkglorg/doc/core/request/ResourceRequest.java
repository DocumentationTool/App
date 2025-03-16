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
	
	public ResourceRequest(String searchTerm,
						   String path,
						   String repoId,
						   String userId,
						   List<String> whiteListTags,
						   List<String> blacklistTags,
						   boolean withData,
						   int returnLimit) {
		this.searchTerm = searchTerm;
		this.path = path;
		this.repoId = repoId;
		this.userId = userId;
		this.whiteListTags = whiteListTags;
		this.blacklistTags = blacklistTags;
		this.withData = withData;
		this.returnLimit = returnLimit;
	}
	
	public ResourceRequest() {
	}
	
	public ResourceRequest(String searchTerm, String path, String repoId, String userId, boolean withData, int returnLimit) {
		this.searchTerm = searchTerm;
		this.path = path;
		this.repoId = repoId;
		this.userId = userId;
		this.withData = withData;
		this.returnLimit = returnLimit;
	}
	
	public String getSearchTerm() {
		return searchTerm;
	}
	
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getRepoId() {
		return repoId;
	}
	
	public void setRepoId(String repoId) {
		this.repoId = repoId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public List<String> getWhiteListTags() {
		return whiteListTags;
	}
	
	public void setWhiteListTags(List<String> whiteListTags) {
		this.whiteListTags = whiteListTags;
	}
	
	public List<String> getBlacklistTags() {
		return blacklistTags;
	}
	
	public void setBlacklistTags(List<String> blacklistTags) {
		this.blacklistTags = blacklistTags;
	}
	
	public boolean isWithData() {
		return withData;
	}
	
	public void setWithData(boolean withData) {
		this.withData = withData;
	}
	
	public int getReturnLimit() {
		return returnLimit;
	}
	
	public void setReturnLimit(int returnLimit) {
		this.returnLimit = returnLimit;
	}
	
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
