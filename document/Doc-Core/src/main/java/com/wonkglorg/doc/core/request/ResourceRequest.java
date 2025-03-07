package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;

import java.util.ArrayList;
import java.util.List;

public class ResourceRequest {
    public String path;
    public String repoId;
    public String userId;
    public List<String> whiteListTags = new ArrayList<>();
    public List<String> blacklistListTags = new ArrayList<>();
    public boolean withData;
    public int returnLimit = 999999999;


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

        if (!hasBlacklist && !hasWhitelist) {
            return true;
        }

        boolean containsWhitelistedTag = hasWhitelist;
        for (Tag tag : tags) {
            if (hasBlacklist && isBlacklistedTag(tag.tagId())) {
                return false;
            }

            if (hasWhitelist && isWhitelistedTag(tag.tagId())) {
                containsWhitelistedTag = true;
            }
        }

        return containsWhitelistedTag;
    }


    public boolean isValidResource(Resource resource) {

        //todo:jmd check path stuff

        if (!hasValidTags(resource.resourceTags())) {
            return false;
        }

        return true;

    }

}
