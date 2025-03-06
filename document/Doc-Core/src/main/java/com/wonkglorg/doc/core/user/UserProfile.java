package com.wonkglorg.doc.core.user;

import com.google.gson.Gson;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * The users profile
 */
public class UserProfile{
	private final Gson gson = new Gson();
	private final UserId id;
	private final String passwordHash;
	private final Set<Permission<UserId>> permissionNodes;
	private final Set<Role> roles;
	
	public UserProfile(UserId id, String passwordHash) {
		this.id = id;
		this.permissionNodes = new HashSet<>();
		this.roles = new HashSet<>();
		this.passwordHash = passwordHash;
	}
	
	public UserProfile(UserId id, String passwordHash, Set<Permission<UserId>> permissionNodes, Set<Role> roles) {
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
	
	public Set<Permission<UserId>> getPermissions() {
		return permissionNodes;
	}
	
	public Set<Role> getRoles() {
		return roles;
	}
	
	@Override
	public String toString() {
		return gson.toJson(this);
	}
}
