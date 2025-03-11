package com.wonkglorg.doc.core.user;

import com.wonkglorg.doc.core.objects.DateHelper;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.permissions.Permission;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Group {
    private final GroupId id;
    private String name;

    private String createdBy;
    private LocalDateTime creationDate;
    private String modifiedBy;
    private LocalDateTime lastModified;
    private final Set<Permission<GroupId>> permissions = new HashSet<>();

    public Group(GroupId id, String name, String createdBy, LocalDateTime creationDate, String modifiedBy, LocalDateTime lastModified) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.modifiedBy = modifiedBy;
        this.lastModified = lastModified;
    }


    public Group(GroupId id, String name, String createdBy, String creationDate, String modifiedBy, String lastModified) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.creationDate = DateHelper.parseDateTime(creationDate);
        this.modifiedBy = modifiedBy;
        this.lastModified = DateHelper.parseDateTime(lastModified);
    }

    public Group(GroupId id, String name, String createdBy, LocalDateTime creationDate) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        lastModified = null;
        modifiedBy = null;
    }

    public GroupId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public Set<Permission<GroupId>> getPermissions() {
        return permissions;
    }
}
