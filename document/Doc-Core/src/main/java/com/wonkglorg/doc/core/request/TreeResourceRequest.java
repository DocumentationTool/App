package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TreeResourceRequest{
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
	public List<String> blacklistListTags = new ArrayList<>();
	/**
	 * If the data of the resource should be returned
	 */
	public boolean withData;
	/**
	 * The limit of results to return
	 */
	public int returnLimit = 999999999;
	public boolean groupByCategory;
	
	private boolean isWhiteListEnabled() {
		return whiteListTags == null || whiteListTags.isEmpty();
	}
	
	private boolean isBlackListEnabled() {
		return blacklistListTags == null || blacklistListTags.isEmpty();
	}
	
	private boolean isWhitelistedTag(String tag) {
		return !isWhiteListEnabled() || whiteListTags.contains(tag);
	}
	
	private boolean isBlacklistedTag(String tag) {
		return !isBlackListEnabled() || blacklistListTags.contains(tag);
	}
	
	/**
	 * Returns true if the list of tags adheres to the set black / whitelist
	 *
	 * @param tags
	 * @return
	 */
	private boolean hasValidTags(List<Tag> tags) {
		boolean hasBlacklist = isBlackListEnabled();
		boolean hasWhitelist = isWhiteListEnabled();
		
		if(!hasBlacklist && !hasWhitelist){
			return true;
		}
		
		boolean containsWhitelistedTag = hasWhitelist;
		for(Tag tag : tags){
			if(hasBlacklist && isBlacklistedTag(tag.tagId())){
				return false;
			}
			
			if(hasWhitelist && isWhitelistedTag(tag.tagId())){
				containsWhitelistedTag = true;
			}
		}
		
		return containsWhitelistedTag;
	}
	
	public boolean isValidResource(Resource resource) {
		
		//todo:jmd check path stuff
		
		if(!hasValidTags(resource.resourceTags())){
			return false;
		}
		
		return true;
		
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof TreeResourceRequest that)){
			return false;
		}
		return withData == that.withData && returnLimit == that.returnLimit && Objects.equals(searchTerm, that.searchTerm) && Objects.equals(path,
				that.path) && Objects.equals(repoId, that.repoId) && Objects.equals(userId, that.userId) && Objects.equals(whiteListTags,
				that.whiteListTags) && Objects.equals(blacklistListTags, that.blacklistListTags);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(searchTerm, path, repoId, userId, whiteListTags, blacklistListTags, withData, returnLimit);
	}
}
