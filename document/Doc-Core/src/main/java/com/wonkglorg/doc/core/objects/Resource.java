package com.wonkglorg.doc.core.objects;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a resource in the database
 */
public final class Resource{
	
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private Path resourcePath;
	private final LocalDateTime createdAt;
	private final String createdBy;
	private final LocalDateTime modifiedAt;
	private final String modifiedBy;
	private final RepoId repoId;
	private final Map<String, Tag> resourceTags;
	private final boolean isEditable;
	private final String category;
	private String data;
	
	public Resource(Path resourcePath,
					LocalDateTime createdAt,
					String createdBy,
					LocalDateTime modifiedAt,
					String modifiedBy,
					RepoId repoId,
					List<Tag> resourceTags,
					boolean isEditable,
					String category,
					String data) {
		Objects.requireNonNull(resourcePath, "A Resources, path cannot be null");
		this.resourcePath = resourcePath;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
		this.repoId = repoId;
		this.resourceTags = resourceTags == null ? new HashMap<>() : resourceTags.stream().collect(HashMap::new,
				(m, v) -> m.put(v.tagId(), v),
				HashMap::putAll);
		this.isEditable = isEditable;
		this.category = category;
		this.data = data;
	}
	
	public Resource(Path resourcePath, String creator, RepoId repoId, String category, List<Tag> tags, String data) {
		this(resourcePath, LocalDateTime.now(), creator, LocalDateTime.now(), creator, repoId, tags, false, category, data);
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
	
	public Map<String, Tag> getResourceTags() {
		return resourceTags;
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
	
	public Resource copy() {
		return new Resource(resourcePath,
				createdAt,
				createdBy,
				modifiedAt,
				modifiedBy,
				repoId,
				new ArrayList<>(resourceTags.values()),
				isEditable,
				category,
				data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		if(obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		var that = (Resource) obj;
		return Objects.equals(this.resourcePath, that.resourcePath) &&
			   Objects.equals(this.createdAt, that.createdAt) &&
			   Objects.equals(this.createdBy, that.createdBy) &&
			   Objects.equals(this.modifiedAt, that.modifiedAt) &&
			   Objects.equals(this.modifiedBy, that.modifiedBy) &&
			   Objects.equals(this.repoId, that.repoId) &&
			   Objects.equals(this.resourceTags, that.resourceTags) &&
			   this.isEditable == that.isEditable &&
			   Objects.equals(this.category, that.category) &&
			   Objects.equals(this.data, that.data);
	}
	
	public Resource setData(String data) {
		this.data = data;
		return this;
	}
	
	public Resource setTags(List<Tag> resourceTags) {
		this.resourceTags.clear();
		resourceTags.forEach(tag -> this.resourceTags.put(tag.tagId(), tag));
		return this;
	}
	
	public Resource setResourcePath(Path resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}
	
	public boolean hasAnyTag(Set<Tag> tags) {
		for(Tag tag : tags){
			if(resourceTags.containsKey(tag.tagId())){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAnyTag(List<String> tagIds) {
		for(String tag : tagIds){
			if(resourceTags.containsKey(tag)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(resourcePath, createdAt, createdBy, modifiedAt, modifiedBy, repoId, resourceTags, isEditable, category, data);
	}
	
	@Override
	public String toString() {
		return "Resource[" +
			   "resourcePath=" +
			   resourcePath +
			   ", " +
			   "createdAt=" +
			   createdAt +
			   ", " +
			   "createdBy=" +
			   createdBy +
			   ", " +
			   "modifiedAt=" +
			   modifiedAt +
			   ", " +
			   "modifiedBy=" +
			   modifiedBy +
			   ", " +
			   "repoId=" +
			   repoId +
			   ", " +
			   "resourceTags=" +
			   resourceTags +
			   ", " +
			   "isEditable=" +
			   isEditable +
			   ", " +
			   "category=" +
			   category +
			   ", " +
			   "data=" +
			   data +
			   ']';
	}
	
}