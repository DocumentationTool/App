package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;

import java.nio.file.Path;
import java.util.List;

import static com.wonkglorg.doc.core.path.TargetPath.normalizePath;

public class ResourceUpdateRequest {

    /**
     * The id of the repository
     */
    private RepoId repoId;
    /**
     * The path to the resource
     */
    private Path path;
    /**
     * The user to update the resource as
     */
    private UserId userId;

    /**
     * The tags to add to the resource
     */
    private List<TagId> tagsToAdd;
    /**
     * The tags to remove from the resource
     */
    private List<TagId> tagsToRemove;
    /**
     * The tags to set on the resource removes any previous tags
     */
    private List<TagId> tagsToSet;

    /**
     * The category to set on the resource
     */
    private String category;

    /**
     * The data to set on the resource
     */
    private String data;

    private boolean treatNullsAsValues = false;


    public String getRepoId() {
        return repoId.id();
    }

    public void setRepoId(String repoId) {
        this.repoId = RepoId.of(repoId);
    }

    public String getPath() {
        return path.toString();
    }

    public void setPath(String path) {
        this.path = Path.of(normalizePath(path));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<TagId> getTagsToAdd() {
        return tagsToAdd;
    }

    public void setTagsToAdd(List<TagId> tagsToAdd) {
        this.tagsToAdd = tagsToAdd;
    }

    public List<TagId> getTagsToRemove() {
        return tagsToRemove;
    }

    public void setTagsToRemove(List<TagId> tagsToRemove) {
        this.tagsToRemove = tagsToRemove;
    }

    public List<TagId> getTagsToSet() {
        return tagsToSet;
    }

    public void setTagsToSet(List<TagId> tagsToSet) {
        this.tagsToSet = tagsToSet;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isTreatNullsAsValues() {
        return treatNullsAsValues;
    }

    public void setTreatNullsAsValues(boolean treatNullsAsValues) {
        this.treatNullsAsValues = treatNullsAsValues;
    }
}
