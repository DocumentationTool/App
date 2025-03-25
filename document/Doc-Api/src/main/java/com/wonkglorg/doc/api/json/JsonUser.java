package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Json representation of a user
 */
public class JsonUser{
	public final String userId;
	public final List<String> groups = new ArrayList<>();
	public final List<Role> roles = new ArrayList<>();
	
	public JsonUser(UserProfile user) {
		this.userId = user.getId().id();
		user.getGroups().forEach(g -> groups.add(g.id()));
		roles.addAll(user.getRoles());
		
	}
}
