package com.wonkglorg.user;

public enum Permission {
    /**
     * Allows full admin control over all Permissions
     */
    ADMIN(),
    /**
     * Allows for editing and viewing the file (editing includes deleting files, or uploading if it is a directory)
     */
    EDIT(),
    /**
     * Allows for viewing but not editing of files
     */
    VIEW(),
    /**
     * Explicitly denies access (this can be used to revoke access in a subfolder or specific file despite overall permissions being granted)
     */
    DENY()
}
