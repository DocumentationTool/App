package com.wonkglorg.doc.core.permissions;

import com.wonkglorg.doc.core.objects.*;

/**
 * Represents a permission
 *
 * @param <T>
 */
public class Permission<T extends Identifyable> {

    /**
     * The User this permission is for
     */
    private final T id;

    /**
     * The permissions associated with this Node
     */
    private PermissionType permission;

    /**
     * The path this resource permission is for
     */
    private ResourcePath path;

    /**
     * The repo this permission belongs to
     */
    private RepoId repoId;

    public Permission(T id, PermissionType permission, ResourcePath path, RepoId repoId) {
        this.id = id;
        this.permission = permission;
        this.path = path;
        this.repoId = repoId;
    }


    public String getId() {
        return id.id();
    }

    public boolean isGroup() {
        return id instanceof GroupId;
    }

    public boolean isUser() {
        return id instanceof UserId;
    }

    public void setPermission(PermissionType permission) {
        this.permission = permission;
    }

    public void setPath(ResourcePath path) {
        this.path = path;
    }

    public void setRepoId(RepoId repoId) {
        this.repoId = repoId;
    }
}
