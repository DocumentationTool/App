package com.wonkglorg.doc.core.user;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;

import java.util.Map;
import java.util.Set;

/**
 * A User found in a repository
 */
public class RepositoryUser extends UserProfile {

    private final Map<String, Permission<UserId>> permissions;
    private final Set<Role> roles;

    public RepositoryUser(UserId id, String password, Map<String, Permission<UserId>> permissionNodes, Set<GroupId> groups, Map<String, Permission<UserId>> permissions, Set<Role> roles) {
        super(id, password, permissionNodes, groups);
        this.permissions = permissions;
        this.roles = roles;
    }
}
