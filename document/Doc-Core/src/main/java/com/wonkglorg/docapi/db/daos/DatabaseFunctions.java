package com.wonkglorg.docapi.db.daos;

import org.jdbi.v3.sqlobject.statement.SqlScript;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface DatabaseFunctions {
	/**
	 * Initialises the SQL Tables
	 */
	@SqlScript("""
    BEGIN TRANSACTION;
    PRAGMA auto_vacuum = INCREMENTAL;
    PRAGMA incremental_vacuum(500);
    PRAGMA foreign_keys = OFF;
    CREATE TABLE IF NOT EXISTS Roles (
                  roleID TEXT PRIMARY KEY,
                  roleName TEXT NOT NULL);
    CREATE TABLE IF NOT EXISTS Users (
                           userID TEXT PRIMARY KEY,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by TEXT,
                           last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           last_modified_by TEXT
    );
    CREATE TABLE IF NOT EXISTS UserRoles (
                               roleID TEXT,
                               userID TEXT,
                               PRIMARY KEY (roleID, userID),
                               FOREIGN KEY (roleID) REFERENCES Roles(roleID),
                               FOREIGN KEY (userID) REFERENCES Users(userID)
    );
    CREATE TABLE IF NOT EXISTS Groups (
                            groupID TEXT PRIMARY KEY,
                            group_name TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            created_by TEXT,
                            last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            last_modified_by TEXT
    );
    CREATE TABLE IF NOT EXISTS GroupUsers (
                                userID TEXT,
                                groupID TEXT,
                                PRIMARY KEY (userID, groupID),
                                FOREIGN KEY (userID) REFERENCES Users(userID),
                                FOREIGN KEY (groupID) REFERENCES Groups(groupID)
    );
    CREATE TABLE IF NOT EXISTS Tags(
                              tag TEXT PRIMARY KEY,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              created_by TEXT
    );
    CREATE TABLE IF NOT EXISTS Resources (
                              resourcePath TEXT PRIMARY KEY,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              created_by TEXT,
                              last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              last_modified_by TEXT
    );
    CREATE TABLE IF NOT EXISTS ResourceTags(
                              tag TEXT,
                              resourcePath TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              created_by TEXT,
                              PRIMARY KEY (tag, resourcePath),
                              FOREIGN KEY (tag) REFERENCES Tags(tag),
                              FOREIGN KEY (resourcePath) REFERENCES Resources(resourcePath)
    );
    CREATE TABLE IF NOT EXISTS GroupPermissions (
                                  groupID TEXT,
                                  path TEXT,
                                  type TEXT,
                                  PRIMARY KEY (groupID, path),
                                  FOREIGN KEY (groupID) REFERENCES Groups(groupID),
                                  FOREIGN KEY (path) REFERENCES Resources(resourcePath),
                                  FOREIGN KEY (type) REFERENCES Permissions(permissionID)
    );
    CREATE TABLE IF NOT EXISTS UserPermissions (
                                 userID TEXT,
                                 path TEXT,
                                 type TEXT,
                                 PRIMARY KEY (userID, path),
                                 FOREIGN KEY (userID) REFERENCES Users(userID),
                                 FOREIGN KEY (path) REFERENCES Resources(resourcePath),
                                 FOREIGN KEY (type) REFERENCES Permissions(permissionID)
    );
    CREATE TABLE IF NOT EXISTS Permissions (
                             permissionID TEXT PRIMARY KEY,
                             weight INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS AuditLog (
                              logID INTEGER PRIMARY KEY AUTOINCREMENT,
                              userID TEXT,
                              action TEXT NOT NULL,
                              permissionID TEXT,
                              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              affected_userID TEXT,
                              affected_groupID TEXT,
                              FOREIGN KEY (userID) REFERENCES Users(userID),
                              FOREIGN KEY (permissionID) REFERENCES Permissions(permissionID),
                              FOREIGN KEY (affected_userID) REFERENCES Users(userID),
                              FOREIGN KEY (affected_groupID) REFERENCES Groups(groupID)
    );
    CREATE TABLE IF NOT EXISTS FileData(
                              resourcePath TEXT PRIMARY KEY,
                              data Text,
                              modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    CREATE VIRTUAL TABLE IF NOT EXISTS FileData_FTS USING fts5(resourcePath, content='FileData',tokenize='trigram'); --content referes to another table to keep indexed
    PRAGMA foreign_keys = ON;
    COMMIT
""")
	void initialize();

	@SuppressWarnings("SqlResolve") //fts5 specific command not recognized but is valid
	@SqlUpdate("INSERT INTO FileData(FileData) VALUES ('rebuild')")
	void rebuildFts();

	/**
	 * Sets up triggers for various usecases
	 */
	@SqlScript("""
			CREATE TRIGGER IF NOT EXISTS delete_resource_cleanup
			AFTER DELETE ON Resources
			FOR EACH ROW
			BEGIN
			   -- Delete related permissions
			    DELETE FROM GroupPermissions WHERE resourcePath = OLD.resourcePath;
			    DELETE FROM UserPermissions WHERE resourcePath = OLD.resourcePath;
			    --Delete Related Tags
			    DELETE FROM ResourceTags WHERE resourcePath = OLD.resourcePath;
			    --Delete Indexed Data
			    DELETE FROM FileData WHERE resourcePath = OLD.resourcePath;
			END;
			
			CREATE TRIGGER IF NOT EXISTS update_resource_path
			    AFTER UPDATE ON Resources
			    FOR EACH ROW
			    WHEN OLD.resourcePath != NEW.resourcePath
			    BEGIN
			    -- Update related permissions
			    UPDATE GroupPermissions SET resourcePath = NEW.resourcePath, last_modified_at = datetime('now') WHERE resourcePath = OLD.resourcePath;
			    UPDATE UserPermissions SET resourcePath = NEW.resourcePath, last_modified_at = datetime('now') WHERE resourcePath = OLD.resourcePath;
			    -- Update related tags
			    UPDATE ResourceTags SET resourcePath = NEW.resourcePath, last_modified_at = datetime('now') WHERE resourcePath = OLD.resourcePath;
			    -- Update indexed data
			    UPDATE FileData SET resourcePath = NEW.resourcePath, last_modified_at = datetime('now') WHERE resourcePath = OLD.resourcePath;
			    END
			""")
	void setupTriggers();

		@SqlScript("""
			CREATE TRIGGER IF NOT EXISTS delete_resource_cleanup
			AFTER DELETE ON Resources
			FOR EACH ROW
			BEGIN
			   -- Delete related permissions
			    DELETE FROM GroupPermissions WHERE resourcePath = OLD.resourcePath;
			    DELETE FROM UserPermissions WHERE resourcePath = OLD.resourcePath;
			    --Delete Related Tags
			    DELETE FROM ResourceTags WHERE resourcePath = OLD.resourcePath;
			    --Delete Indexed Data
			    DELETE FROM FileData WHERE resourcePath = OLD.resourcePath;
			END;
			""")
	void setupTriggersTest();
}
