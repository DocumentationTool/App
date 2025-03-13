package com.wonkglorg.doc.core.objects;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A repos unique identifier
 */
public record RepoId(String id) implements Identifyable{
	
	/**
	 * A special id that represents all repos
	 */
	public static RepoId ALL_REPOS = new RepoId("0000-0000-0000-0000");
	
	public static RepoId of(String id) {
		return new RepoId(id);
	}
	
	/**
	 * Checks if this id represents all repos
	 */
	public boolean isAllRepos() {
		return this.equals(ALL_REPOS);
	}
	
	/**
	 * Filters all matching repos or lets all pass if id is null
	 */
	public Predicate<RepoId> filter() {
		return (RepoId repoId) -> id == null || repoId.equals(this);
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof RepoId id1)){
			return false;
		}
		return Objects.equals(id, id1.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
