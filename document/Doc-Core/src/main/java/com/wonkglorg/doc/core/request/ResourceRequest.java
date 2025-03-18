package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResourceRequest{
	/**
	 * The term to search text by
	 */
	public String searchTerm;
	/**
	 * The path to search by may be an ant path
	 */
	public TargetPath path;
	/**
	 * The repo to search in
	 */
	public RepoId repoId;
	/**
	 * The user to search limit the search to
	 */
	public UserId userId;
	/**
	 * The tags to search by
	 */
	public List<TagId> whiteListTags = new ArrayList<>();
	/**
	 * The tags to exclude from the search
	 */
	public List<TagId> blacklistTags = new ArrayList<>();
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
		this.path = new TargetPath(path);
		this.repoId = RepoId.of(repoId);
		this.userId = UserId.of(userId);
		this.whiteListTags = whiteListTags.stream().map(TagId::new).collect(Collectors.toList());
		this.blacklistTags = blacklistTags.stream().map(TagId::new).collect(Collectors.toList());
		this.withData = withData;
		this.returnLimit = returnLimit;
	}
	
	public ResourceRequest() {
	}
	
	public ResourceRequest(String searchTerm, String path, String repoId, String userId, boolean withData, int returnLimit) {
		this.searchTerm = searchTerm;
		this.path = new TargetPath(path);
		this.repoId = RepoId.of(repoId);
		this.userId = UserId.of(userId);
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
		return path.toString();
	}
	
	public void setPath(String path) {
		this.path = new TargetPath(path);
	}
	
	public String getRepoId() {
		return repoId.id();
	}
	
	public void setRepoId(String repoId) {
		this.repoId = RepoId.of(repoId);
	}
	
	public String getUserId() {
		return userId.id();
	}
	
	public void setUserId(String userId) {
		this.userId = UserId.of(userId);
	}

	//todo:jmd implement real requests as RepoId repoId() void repoId(RepoId id);

	//todo:jmd implement setter and getter for basic types, String getRepoId() void setRepoId(String id);

	
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
