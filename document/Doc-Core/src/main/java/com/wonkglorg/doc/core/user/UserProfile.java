package com.wonkglorg.doc.core.user;

import com.google.gson.Gson;
import com.wonkglorg.doc.core.objects.Identifyable;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.ArrayList;
import java.util.List;

public class UserProfile{
	private final Gson gson = new Gson();
	private final UserId id;
	private final String passwordHash;
	private final List<Permission<Identifyable>> permissionNodes;
	private final List<Role> roles;
	
	public UserProfile(UserId id, String passwordHash) {
		this.id = id;
		this.permissionNodes = new ArrayList<>();
		this.roles = new ArrayList<>();
		this.passwordHash = passwordHash;
	}
	
	public UserProfile(UserId id, String passwordHash, List<Permission<Identifyable>> permissionNodes, List<Role> roles) {
		this.id = id;
		this.permissionNodes = permissionNodes;
		this.roles = roles;
		this.passwordHash = passwordHash;
	}
	
	public UserId getId() {
		return id;
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public List<Permission<Identifyable>> getPermissions() {
		return permissionNodes;
	}
	
	public List<Role> getRoles() {
		return roles;
	}
	
	@Override
	public String toString() {
		return gson.toJson(this);
	}
}
