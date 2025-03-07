package com.wonkglorg.doc.core.objects;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a resource in the database
 */
public final class Resource {

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path resourcePath;
    private final LocalDateTime createdAt;
    private final String createdBy;
    private final LocalDateTime modifiedAt;
    private final String modifiedBy;
    private final RepoId repoId;
    private final List<Tag> resourceTags;
    private final String commitId;
    private final boolean isEditable;
    private final String category;
    private String data;


    public Resource(Path resourcePath, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, RepoId repoId, List<Tag> resourceTags, String commitId, boolean isEditable, String category, String data) {
        Objects.requireNonNull(resourcePath, "A Resources, path cannot be null");
        this.resourcePath = resourcePath;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
        this.repoId = repoId;
        this.resourceTags = resourceTags == null ? new ArrayList<>() : resourceTags;
        this.commitId = commitId;
        this.isEditable = isEditable;
        this.category = category;
        this.data = data;
    }

    public Resource(Path resourcePath, String creator, RepoId repoId, String commitId, boolean isEditable, String data) {
        this(resourcePath, LocalDateTime.now(), creator, LocalDateTime.now(), creator, repoId, List.of(), commitId, isEditable, null, data);
    }


    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, formatter);
    }

    public static String fromDateTime(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }

    public String getModifiedAt() {
        return fromDateTime(modifiedAt);
    }

    public String getCreatedAt() {
        return fromDateTime(createdAt);
    }

    public Path resourcePath() {
        return resourcePath;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public String createdBy() {
        return createdBy;
    }

    public LocalDateTime modifiedAt() {
        return modifiedAt;
    }

    public String modifiedBy() {
        return modifiedBy;
    }

    public RepoId repoId() {
        return repoId;
    }

    public List<Tag> resourceTags() {
        return resourceTags;
    }

    public String commitId() {
        return commitId;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public String category() {
        return category;
    }

    public String data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Resource) obj;
        return Objects.equals(this.resourcePath, that.resourcePath) &&
                Objects.equals(this.createdAt, that.createdAt) &&
                Objects.equals(this.createdBy, that.createdBy) &&
                Objects.equals(this.modifiedAt, that.modifiedAt) &&
                Objects.equals(this.modifiedBy, that.modifiedBy) &&
                Objects.equals(this.repoId, that.repoId) &&
                Objects.equals(this.resourceTags, that.resourceTags) &&
                Objects.equals(this.commitId, that.commitId) &&
                this.isEditable == that.isEditable &&
                Objects.equals(this.category, that.category) &&
                Objects.equals(this.data, that.data);
    }


    public void setData(String data) {
        this.data = data;
    }

    public void setTags(List<Tag> resourceTags) {
        this.resourceTags.clear();
        this.resourceTags.addAll(resourceTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourcePath, createdAt, createdBy, modifiedAt, modifiedBy, repoId, resourceTags, commitId, isEditable, category, data);
    }

    @Override
    public String toString() {
        return "Resource[" +
                "resourcePath=" + resourcePath + ", " +
                "createdAt=" + createdAt + ", " +
                "createdBy=" + createdBy + ", " +
                "modifiedAt=" + modifiedAt + ", " +
                "modifiedBy=" + modifiedBy + ", " +
                "repoId=" + repoId + ", " +
                "resourceTags=" + resourceTags + ", " +
                "commitId=" + commitId + ", " +
                "isEditable=" + isEditable + ", " +
                "category=" + category + ", " +
                "data=" + data + ']';
    }


}