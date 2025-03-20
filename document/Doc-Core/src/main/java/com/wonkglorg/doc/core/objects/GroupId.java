package com.wonkglorg.doc.core.objects;

import java.util.Objects;

/**
 * A groups unique identifier
 */
public class GroupId implements Identifyable{
	/**
	 * Indicates all groups should be selected
	 */
	public static final GroupId ALL_GROUPS = new GroupId("0000-0000-0000-0000_ALL_GROUPS");
	
	private final String id;
	
	private GroupId(String id) {
		this.id = id;
	}
	
	public static GroupId of(String id) {
		return id == null ? ALL_GROUPS : new GroupId(id);
	}
	
	
	public boolean isAllGroups() {
		return this.equals(ALL_GROUPS);
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	@Override
	public String id() {
		return id;
	}


	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		GroupId groupId = (GroupId) o;
		return Objects.equals(id, groupId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
