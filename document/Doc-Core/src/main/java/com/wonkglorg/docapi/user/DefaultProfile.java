package com.wonkglorg.docapi.user;


import com.wonkglorg.docapi.permissions.PermissionNode;
import com.wonkglorg.docapi.permissions.Role;

import java.util.ArrayList;
import java.util.List;

public class DefaultProfile implements UserProfile {

    private final String userName;
    private final List<PermissionNode> permissionNodes;
	private final List<Role> roles;
	
	public DefaultProfile(String userName) {
        this.userName = userName;
        this.permissionNodes = new ArrayList<>();
        this.roles = new ArrayList<>();
    }

    public DefaultProfile(String userName, List<PermissionNode> permissionNodes, List<Role> roles) {
        this.userName = userName;
        this.permissionNodes = permissionNodes;
		this.roles = roles;
	}

    public static DefaultProfile createDefault() {
        return new DefaultProfile("TestProfile", List.of(), List.of());
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPasswordHash() {
        return "";
    }

    @Override
    public List<PermissionNode> permissions() {
        return permissionNodes;
    }
    
    @Override
    public List<Role> roles() {
        return roles;
    }
    
    @Override
    public String toString() {
        return "DefaultProfile{" +
                "userName='" + userName + '\'' +
                ", permissionNodes=" + permissionNodes +
                '}';
    }
}
