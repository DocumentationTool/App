package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A request received by a controller and used for file getting requests from the database
 */
public class ResourceRequest{
	/**
	 * The term to search text by
	 */
	private String searchTerm = null;
	/**
	 * The path to search by may be an ant path
	 */
	private TargetPath path = TargetPath.of((String) null);
	/**
	 * The repo to search in
	 */
	private RepoId repoId = null;
	/**
	 * The user to search limit the search to
	 */
	private UserId userId = null;
	/**
	 * The tags to search by
	 */
	private Set<TagId> whitelistTags = new HashSet<>();
	/**
	 * The tags to exclude from the search
	 */
	private Set<TagId> blacklistTags = new HashSet<>();
	/**
	 * If the data of the resource should be returned
	 */
	private boolean withData = false;
	/**
	 * The limit of results to return
	 */
	private int returnLimit = 999999999;
	
	
	public TargetPath targetPath() {
		return path;
	}
	
	public RepoId repoId() {
		return repoId;
	}
	
	public UserId userId() {
		return userId;
	}
	
	public Set<TagId> whiteListTags() {
		return whitelistTags;
	}
	
	public Set<TagId> blacklistTags() {
		return blacklistTags;
	}
	
	public void targetPath(TargetPath path) {
		if(path == null){
			this.path = TargetPath.of((String) null);
			return;
		}
		this.path = path;
	}
	
	public void repoId(RepoId repoId) {
		this.repoId = repoId;
	}
	
	public void userId(UserId userId) {
		this.userId = userId;
	}
	
	public void whiteListTags(Set<TagId> whiteListTags) {
		this.whitelistTags = whiteListTags;
	}
	
	public void blacklistTags(Set<TagId> blacklistTags) {
		this.blacklistTags = blacklistTags;
	}
	
	
	
	
	//-----mappings for rest objects
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
		this.path = TargetPath.of(path);
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
	
	public Set<String> getWhitelistTags() {
		return whitelistTags.stream().toList().stream().map(TagId::id).collect(Collectors.toSet());
	}
	
	public void setWhitelistTags(Set<String> whitelistTags) {
		this.whitelistTags = whitelistTags.stream().map(TagId::of).collect(Collectors.toSet());
	}
	
	public Set<String> getBlacklistTags() {
		return blacklistTags.stream().map(TagId::id).collect(Collectors.toSet());
	}
	
	public void setBlacklistTags(Set<String> blacklistTags) {
		this.blacklistTags = blacklistTags.stream().map(TagId::of).collect(Collectors.toSet());
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
				that.path) && Objects.equals(repoId, that.repoId) && Objects.equals(userId, that.userId) && Objects.equals(whitelistTags,
				that.whitelistTags) && Objects.equals(blacklistTags, that.blacklistTags);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(searchTerm, path, repoId, userId, whitelistTags, blacklistTags, withData, returnLimit);
	}
}
