package com.wonkglorg.doc.core.request;

import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import static com.wonkglorg.doc.core.path.TargetPath.normalizePath;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceUpdateRequest{
	
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
	private Set<TagId> tagsToAdd;
	/**
	 * The tags to remove from the resource
	 */
	private Set<TagId> tagsToRemove;
	/**
	 * The tags to set on the resource removes any previous tags
	 */
	private Set<TagId> tagsToSet;
	
	/**
	 * The category to set on the resource
	 */
	private String category;
	
	/**
	 * The data to set on the resource
	 */
	private String data;
	
	private boolean treatNullsAsValues = false;
	
	
	public RepoId repoId() {
        return repoId;
    }
	
	public Path path() {
        return path;
    }
	
	public UserId userId() {
        return userId;
    }
	
	public Set<TagId> tagsToAdd() {
        return tagsToAdd;
    }
	
	public Set<TagId> tagsToRemove() {
        return tagsToRemove;
    }
	
	public Set<TagId> tagsToSet() {
        return tagsToSet;
    }
	
	
	public void repoId(RepoId repoId) {
        this.repoId = repoId;
    }
	
	public void path(Path path) {
        this.path = path;
    }
	
	public void userId(UserId userId) {
        this.userId = userId;
    }
	
	public void tagsToAdd(Set<TagId> tagsToAdd) {
        this.tagsToAdd = tagsToAdd;
    }
	
	public void tagsToRemove(Set<TagId> tagsToRemove) {
        this.tagsToRemove = tagsToRemove;
    }
	
	public void tagsToSet(Set<TagId> tagsToSet) {
        this.tagsToSet = tagsToSet;
    }
	
	
	//-----mappings for rest objects
	
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
		return userId.id();
	}
	
	public void setUserId(String userId) {
		this.userId = UserId.of(userId);
	}
	
	public Set<String> getTagsToAdd() {
		return tagsToAdd.stream().map(TagId::id).collect(Collectors.toSet());
	}
	
	public void setTagsToAdd(Set<String> tagsToAdd) {
		this.tagsToAdd = tagsToAdd.stream().map(TagId::new).collect(Collectors.toSet());
	}
	
	public Set<String> getTagsToRemove() {
		return tagsToRemove.stream().map(TagId::id).collect(Collectors.toSet());
	}
	
	public void setTagsToRemove(Set<String> tagsToRemove) {
		this.tagsToRemove = tagsToRemove.stream().map(TagId::new).collect(Collectors.toSet());
	}
	
	public Set<String> getTagsToSet() {
		return tagsToSet.stream().map(TagId::id).collect(Collectors.toSet());
	}
	
	public void setTagsToSet(Set<String> tagsToSet) {
		this.tagsToSet = tagsToSet.stream().map(TagId::new).collect(Collectors.toSet());
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
