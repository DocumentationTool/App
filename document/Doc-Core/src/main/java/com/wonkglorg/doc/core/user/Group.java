package com.wonkglorg.doc.core.user;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.permissions.Permission;

import java.util.HashSet;
import java.util.Set;

public class Group{
	private final GroupId id;
	private final Set<Permission<GroupId>> permissions = new HashSet<>();
	
	public Group(GroupId id) {
		this.id = id;
	}
	
	
}
