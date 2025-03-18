package com.wonkglorg.doc.api.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;

import java.util.List;

public class TestRequest {

    /**
     * The id of the repository
     */
    private RepoId repoId;
    /**
     * The path to the resource
     */
    private String path;
    /**
     * The user to update the resource as
     */
    private UserId userId;

    /**
     * The tags to add to the resource
     */
    private List<String> tagsToAdd;
    /**
     * The tags to remove from the resource
     */
    private List<String> tagsToRemove;
    /**
     * The tags to set on the resource removes any previous tags
     */
    private List<String> tagsToSet;

    /**
     * The category to set on the resource
     */
    private String category;

    /**
     * The data to set on the the resource
     */
    private String data;

    private boolean treatNullsAsValues = false;


    public TestRequest(String repoId, String path, String userId, List<String> tagsToAdd, List<String> tagsToRemove, List<String> tagsToSet, String category, String data, boolean treatNullsAsValues) {
        this.repoId = RepoId.of(repoId);
        this.path = path;
        this.userId = UserId.of(userId);
        this.tagsToAdd = tagsToAdd;
        this.tagsToRemove = tagsToRemove;
        this.tagsToSet = tagsToSet;
        this.category = category;
        this.data = data;
        this.treatNullsAsValues = treatNullsAsValues;
    }


    public String getRepoId() {
        return repoId.id();
    }

    //todo:jmd  json for request params only uses the get methods not any others so I can force the correct ons to show up.!!!
    //@JsonIgnore
    public RepoId repoId() {
        return repoId;
    }

    public String getPath() {
        return path;
    }

    public String getUserId() {
        return userId.id();
    }



    public List<String> getTagsToAdd() {
        return tagsToAdd;
    }

    public List<String> getTagsToRemove() {
        return tagsToRemove;
    }

    public List<String> getTagsToSet() {
        return tagsToSet;
    }

    public String getCategory() {
        return category;
    }

    public String getData() {
        return data;
    }

    public boolean isTreatNullsAsValues() {
        return treatNullsAsValues;
    }
}
