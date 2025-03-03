package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.response.ScriptDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static com.wonkglorg.doc.core.db.builder.StatementBuilder.script;
import static com.wonkglorg.doc.core.db.builder.StatementBuilder.update;

/**
 * Holds generic setup and usage Database functions
 */
@SuppressWarnings("UnusedReturnValue")
public class DatabaseFunctions {
    private static final Logger log = LoggerFactory.getLogger(DatabaseFunctions.class);

    private DatabaseFunctions() {
        //utility class
    }

    /**
     * Initializes the database with the required tables
     *
     * @return {@link ScriptDatabaseResponse}
     */
    public static ScriptDatabaseResponse initializeDatabase(Connection connection) {
        String sqlScript = """
                    PRAGMA auto_vacuum = INCREMENTAL;
                    PRAGMA incremental_vacuum(500);
                    PRAGMA foreign_keys = OFF;
                
                    CREATE TABLE IF NOT EXISTS Roles (
                        role_id TEXT PRIMARY KEY NOT NULL,
                        role_name TEXT NOT NULL
                    );
                
                    CREATE TABLE IF NOT EXISTS Users (
                        user_id TEXT PRIMARY KEY NOT NULL,
                        password_hash TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT,
                        last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_modified_by TEXT
                    );
                
                    CREATE TABLE IF NOT EXISTS UserRoles (
                        role_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        PRIMARY KEY (role_id, user_id),
                        FOREIGN KEY (role_id) REFERENCES Roles(role_id),
                        FOREIGN KEY (user_id) REFERENCES Users(user_id)
                    );
                
                    CREATE TABLE IF NOT EXISTS Groups (
                        group_id TEXT PRIMARY KEY NOT NULL,
                        group_name TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT,
                        last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_modified_by TEXT
                    );
                
                    CREATE TABLE IF NOT EXISTS GroupUsers (
                        user_id TEXT NOT NULL,
                        group_id TEXT NOT NULL,
                        PRIMARY KEY (user_id, group_id),
                        FOREIGN KEY (user_id) REFERENCES Users(user_id),
                        FOREIGN KEY (group_id) REFERENCES Groups(group_id)
                    );
                
                    CREATE TABLE IF NOT EXISTS Tags (
                        tag_id TEXT PRIMARY KEY NOT NULL,
                        tag_name TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT
                    );
                
                    CREATE TABLE IF NOT EXISTS Resources (
                        resource_path TEXT PRIMARY KEY NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT,
                        last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_modified_by TEXT,
                        category TEXT,
                        commit_id TEXT
                    );
                
                    CREATE TABLE IF NOT EXISTS ResourceTags (
                        tag_id TEXT NOT NULL,
                        resource_path TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT,
                        PRIMARY KEY (tag_id, resource_path),
                        FOREIGN KEY (tag_id) REFERENCES Tags(tag_id),
                        FOREIGN KEY (resource_path) REFERENCES Resources(resource_path)
                    );
                
                    CREATE TABLE IF NOT EXISTS Permissions (
                        permission_id TEXT PRIMARY KEY NOT NULL,
                        weight INTEGER NOT NULL
                    );
                
                    CREATE TABLE IF NOT EXISTS AuditLog (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id TEXT,
                        action TEXT NOT NULL,
                        permission_id TEXT,
                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        affected_userID TEXT,
                        affected_groupID TEXT,
                        FOREIGN KEY (user_id) REFERENCES Users(user_id),
                        FOREIGN KEY (permission_id) REFERENCES Permissions(permission_id),
                        FOREIGN KEY (affected_userID) REFERENCES Users(user_id),
                        FOREIGN KEY (affected_groupID) REFERENCES Groups(group_id)
                    );
                
                    CREATE VIRTUAL TABLE IF NOT EXISTS FileData USING fts5(
                        resource_path,
                        data,
                        tokenize='trigram'
                    );
                
                    PRAGMA foreign_keys = ON;
                """;

        try {
            //noinspection LanguageMismatch
            script(sqlScript).execute(connection);
            return ScriptDatabaseResponse.success();
        } catch (Exception e) {
            String errorResponse = "Error while initializing the database";
            log.error(errorResponse, e);
            return ScriptDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }

    /**
     * Rebuilds the FTS table when called, this is a slow operation and should only be done when there is a specific need to do so
     */
    public static UpdateDatabaseResponse rebuildFts(Connection connection) {
        try {
            //noinspection SqlResolve
            Integer i = update("INSERT INTO FileData(FileData) VALUES ('rebuild')").execute(connection);
            return UpdateDatabaseResponse.success(i);
        } catch (Exception e) {
            String errorResponse = "Error while rebuilding FTS";
            log.error(errorResponse, e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }

    /**
     * Creates a trigger that updates related resource paths when the main "Resources" Table is updated
     *
     * @return {@link ScriptDatabaseResponse}
     */
    public static ScriptDatabaseResponse initializeResourceUpdateTrigger(Connection connection) {
        String sqlScript = """
                CREATE TRIGGER IF NOT EXISTS update_resource_path
                AFTER UPDATE ON Resources
                FOR EACH ROW
                WHEN OLD.resource_path != NEW.resource_path
                BEGIN
                    -- Update related permissions
                    UPDATE GroupPermissions SET resource_path = NEW.resource_path, last_modified_at = datetime('now') WHERE resource_path = OLD.resource_path;
                    UPDATE UserPermissions SET resource_path = NEW.resource_path, last_modified_at = datetime('now') WHERE resource_path = OLD.resource_path;
                    -- Update related tags
                    UPDATE ResourceTags SET resource_path = NEW.resource_path, last_modified_at = datetime('now') WHERE resource_path = OLD.resource_path;
                    -- Update indexed data
                    UPDATE FileData SET resource_path = NEW.resource_path, last_modified_at = datetime('now') WHERE resource_path = OLD.resource_path;
                END;
                """;
        try {
            //noinspection ConstantExpression,LanguageMismatch
            script(sqlScript).execute(connection);
            return ScriptDatabaseResponse.success();
        } catch (Exception e) {
            String errorResponse = "Error while setting up resource update trigger";
            log.error(errorResponse, e);
            return ScriptDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }

    /**
     * Creates a trigger that deletes all accompanying tables resources when the main "Resources" table gets deleted
     *
     * @return {@link ScriptDatabaseResponse}
     */
    public static ScriptDatabaseResponse initializeResourceDeleteTrigger(Connection connection) {
        String sqlScript = """
                CREATE TRIGGER IF NOT EXISTS delete_resource_cleanup
                AFTER DELETE ON Resources
                FOR EACH ROW
                BEGIN
                   -- Delete related permissions
                    DELETE FROM GroupPermissions WHERE resource_path = OLD.resource_path;
                    DELETE FROM UserPermissions WHERE resource_path = OLD.resource_path;
                    --Delete Related Tags
                    DELETE FROM ResourceTags WHERE resource_path = OLD.resource_path;
                    --Delete Indexed Data
                    DELETE FROM FileData WHERE resource_path = OLD.resource_path;
                END;
                """;
        try {
            //noinspection ConstantExpression,LanguageMismatch
            script(sqlScript).execute(connection);
            return ScriptDatabaseResponse.success();
        } catch (Exception e) {
            String errorResponse = "Error while setting up resource remove trigger";
            log.error(errorResponse, e);
            return ScriptDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }



    //todo:jmd implement
    public static UpdateDatabaseResponse logChange(RepositoryDatabase database) {
        try(var statement = database.getConnection().prepareStatement("")) {

        }
    }

}
