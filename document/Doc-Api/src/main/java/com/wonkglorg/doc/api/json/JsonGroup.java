package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.user.Group;

import java.util.ArrayList;
import java.util.List;

public class JsonGroup{
	public final String groupId;
	public final String name;
	public final List<JsonPermission> permissions = new ArrayList<>();
	public final List<String> users = new ArrayList<>();
	
	public JsonGroup(Group group) {
		this.groupId = group.getId().id();
		this.name = group.getName();
		group.getPermissions().values().forEach(p -> permissions.add(new JsonPermission(p)));
		group.getUserIds().forEach(u -> users.add(u.id()));
	}
}
