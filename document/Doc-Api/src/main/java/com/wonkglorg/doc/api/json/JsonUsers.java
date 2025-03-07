package com.wonkglorg.doc.api.json;

import java.util.HashMap;
import java.util.Map;

public class JsonUsers{
	
	public final Map<String, JsonUser> users = new HashMap<>();
	
	private class JsonUser{
		
		public final String userId;
		public final JsonRoles roles;
		public final JsonPermissions permissions;
		
		public JsonUser(String userId, JsonRoles roles, JsonPermissions permissions) {
			this.userId = userId;
			this.roles = roles;
			this.permissions = permissions;
		}
	}
}
