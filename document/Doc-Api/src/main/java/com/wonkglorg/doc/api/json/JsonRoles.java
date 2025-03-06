package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.permissions.Role;

import java.util.ArrayList;
import java.util.List;

public class JsonRoles{
	public final List<JsonRole> roles = new ArrayList<>();
	
	public JsonRoles(List<Role> roles) {
		roles.forEach(r -> this.roles.add(new JsonRole(r.roleID().id(), r.roleName())));
	}
	
	private class JsonRole{
		public String id;
		public String name;
		
		public JsonRole(String id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
