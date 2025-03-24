package com.wonkglorg.doc.core.user;

import com.wonkglorg.doc.core.hash.BCryptUtils;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a users profile
 */
public class UserProfile{
	private final UserId id;
	private final String passwordHash;
	private final Set<GroupId> groups = new HashSet<>();
	private final Set<Role> roles = new HashSet<>();
	
	public UserProfile(UserId id, String password, Set<GroupId> groups, Set<Role> roles) {
		this.id = id;
		this.passwordHash = password;
		if(groups != null){
			this.groups.addAll(groups);
		}
		if(roles != null){
			this.roles.addAll(roles);
		}
	}
	
	public UserId getId() {
		return id;
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	/**
	 * Check if the password hash matches the given password
	 *
	 * @param password the password to check
	 * @return true if the password hash matches the given password
	 */
	public boolean hashMatches(String password) {
		return BCryptUtils.verifyPassword(password, passwordHash);
	}
	
	public Set<GroupId> getGroups() {
		return groups;
	}
	
	public Set<Role> getRoles() {
		return roles;
	}
}
