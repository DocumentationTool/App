package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.user.UserProfile;

public class JsonUser{
	
	public final String userId;
	public final JsonRoles roles;
	public final JsonPermissions permissions;
	
	public JsonUser(String userId, JsonRoles roles, JsonPermissions permissions) {
		this.userId = userId;
		this.roles = roles;
		this.permissions = permissions;
	}
	
	public JsonUser(UserProfile userProfile) {
		this.userId = userProfile.getId().id();
		this.roles = new JsonRoles(userProfile.getRoles());
		this.permissions = new JsonPermissions(userProfile.getPermissions());
	}
}
