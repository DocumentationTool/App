package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.response.ScriptDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import jdk.jshell.spi.ExecutionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;

/**
 * Holds generic setup and usage Database functions
 */
@SuppressWarnings("UnusedReturnValue")
public class DatabaseFunctions{
	private static final Logger log = LoggerFactory.getLogger(DatabaseFunctions.class);
	
	private DatabaseFunctions() {
		//utility class
	}
	
	/**
	 * Initializes the database with the required tables
	 *
	 * @return {@link ScriptDatabaseResponse}
	 */
	public static ScriptDatabaseResponse initializeDatabase(RepositoryDatabase database) {
		try(Statement statement = database.getConnection().createStatement()){
			statement.execute("PRAGMA foreign_keys = OFF");
			statement.execute("PRAGMA auto_vacuum = INCREMENTAL");
			statement.execute("PRAGMA incremental_vacuum(500)");
			statement.execute("""
					CREATE TABLE IF NOT EXISTS Roles (
					    role_id TEXT PRIMARY KEY NOT NULL,
					    role_name TEXT NOT NULL
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS Users (
					    user_id TEXT PRIMARY KEY NOT NULL,
					    password_hash TEXT NOT NULL,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    last_modified_by TEXT
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS UserRoles (
					    role_id TEXT NOT NULL,
					    user_id TEXT NOT NULL,
					    PRIMARY KEY (role_id, user_id),
					    FOREIGN KEY (role_id) REFERENCES Roles(role_id),
					    FOREIGN KEY (user_id) REFERENCES Users(user_id)
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS Groups (
					    group_id TEXT PRIMARY KEY NOT NULL,
					    group_name TEXT NOT NULL,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    last_modified_by TEXT
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS UserGroups (
					    user_id TEXT NOT NULL,
					    group_id TEXT NOT NULL,
					    PRIMARY KEY (user_id, group_id),
					    FOREIGN KEY (user_id) REFERENCES Users(user_id),
					    FOREIGN KEY (group_id) REFERENCES Groups(group_id)
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS Tags (
					    tag_id TEXT PRIMARY KEY NOT NULL,
					    tag_name TEXT,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS Resources (
					    resource_path TEXT PRIMARY KEY NOT NULL,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    last_modified_by TEXT,
					    category TEXT
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS ResourceTags (
					    tag_id TEXT NOT NULL,
					    resource_path TEXT NOT NULL,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT,
					    PRIMARY KEY (tag_id, resource_path),
					    FOREIGN KEY (tag_id) REFERENCES Tags(tag_id),
					    FOREIGN KEY (resource_path) REFERENCES Resources(resource_path)
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS Permissions (
					    permission_id TEXT PRIMARY KEY NOT NULL,
					    weight INTEGER NOT NULL
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS GroupPermissions(
					    group_id TEXT NOT NULL,
					    path TEXT NOT NULL,
					    type TEXT NOT NULL,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    last_modified_by TEXT,
					    PRIMARY KEY (group_id, path)
					)
					""");
			
			statement.execute("""
					CREATE TABLE IF NOT EXISTS UserPermissions(
					    user_id TEXT NOT NULL,
					    path TEXT NOT NULL,
					    type TEXT NOT NULL,
					    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					    last_modified_by TEXT,
					    PRIMARY KEY (user_id, path)
					)
					""");
			
			statement.execute("""
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
					)
					""");
			
			statement.execute("""
					CREATE VIRTUAL TABLE IF NOT EXISTS FileData USING fts5(
					    resource_path,
					    data,
					    tokenize='trigram'
					)
					""");

			statement.execute("""
					CREATE VIEW IF NOT EXISTS ResourceInfo AS
					SELECT Resources.*, ResourceTags.tag_id IS NOT NULL AS hasTags
					  FROM Resources
					  LEFT JOIN ResourceTags
					    ON Resources.resource_path = ResourceTags.resource_path
					  GROUP BY Resources.resource_path;
					""");
			
			//inserts default users
			statement.execute("INSERT OR IGNORE INTO  Roles(role_id, role_name) VALUES ('admin', 'Admin')");
			statement.execute("INSERT OR IGNORE INTO Roles(role_id, role_name) VALUES ('user', 'User')");
			
			statement.execute("PRAGMA foreign_keys = ON");
			return ScriptDatabaseResponse.success(database.getRepoId());
		} catch(Exception e){
			String errorResponse = "Error while initializing the database";
			log.error(errorResponse, e);
			return ScriptDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
	
	/**
	 * Rebuilds the FTS table when called, this is a slow operation and should only be done when there is a specific need to do so
	 */
	public static UpdateDatabaseResponse rebuildFts(RepositoryDatabase database) {
		try(Statement statement = database.getConnection().createStatement()){
			//noinspection SqlResolve on purpose sql plugin doesn't recognize the fts specific commands
			int i = statement.executeUpdate(("INSERT INTO FileData(FileData) VALUES ('rebuild')"));
			return UpdateDatabaseResponse.success(database.getRepoId(), i);
		} catch(Exception e){
			String errorResponse = "Error while rebuilding FTS";
			log.error(errorResponse, e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
	
	/**
	 * Creates a trigger that updates related resource paths when the main "Resources" Table is updated
	 *
	 * @return {@link ScriptDatabaseResponse}
	 */
	public static ScriptDatabaseResponse initializeResourceUpdateTrigger(RepositoryDatabase database) {
		String sqlScript = """
				CREATE TRIGGER IF NOT EXISTS update_resource_path
				AFTER UPDATE ON Resources
				FOR EACH ROW
				WHEN OLD.resource_path != NEW.resource_path
				BEGIN
				    -- Update related permissions
				    UPDATE GroupPermissions SET path = NEW.resource_path, last_modified_at = datetime('now') WHERE path = OLD.resource_path;
				    UPDATE UserPermissions SET path = NEW.resource_path, last_modified_at = datetime('now') WHERE path = OLD.resource_path;
				    -- Update related tags
				    UPDATE ResourceTags SET resource_path = NEW.resource_path WHERE resource_path = OLD.resource_path;
				    -- Update indexed data
				    UPDATE FileData SET resource_path = NEW.resource_path WHERE resource_path = OLD.resource_path;
				END;
				""";
		try(Statement statement = database.getConnection().createStatement()){
			//noinspection ConstantExpression,LanguageMismatch
			statement.execute(sqlScript);
			return ScriptDatabaseResponse.success(database.getRepoId());
		} catch(Exception e){
			String errorResponse = "Error while setting up resource update trigger";
			log.error(errorResponse, e);
			return ScriptDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
	
	/**
	 * Creates a trigger that deletes all accompanying tables resources when the main "Resources" table gets deleted
	 *
	 * @return {@link ScriptDatabaseResponse}
	 */
	public static ScriptDatabaseResponse initializeResourceDeleteTrigger(RepositoryDatabase database) {
		String sqlScript = """
				CREATE TRIGGER IF NOT EXISTS delete_resource_cleanup
				AFTER DELETE ON Resources
				FOR EACH ROW
				BEGIN
				   -- Delete related permissions
				    DELETE FROM GroupPermissions WHERE path = OLD.resource_path;
				    DELETE FROM UserPermissions WHERE path = OLD.resource_path;
				    --Delete Related Tags
				    DELETE FROM ResourceTags WHERE resource_path = OLD.resource_path;
				    --Delete Indexed Data
				    DELETE FROM FileData WHERE resource_path = OLD.resource_path;
				END;
				""";
		try(Statement statement = database.getConnection().createStatement()){
			statement.execute(sqlScript);
			return ScriptDatabaseResponse.success(database.getRepoId());
		} catch(Exception e){
			String errorResponse = "Error while setting up resource remove trigger";
			log.error(errorResponse, e);
			return ScriptDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
	
	
	
	//todo:jmd implement
	public static UpdateDatabaseResponse logChange(RepositoryDatabase database) {
        /*
        try (var statement = database.getConnection().prepareStatement("")) {}
        */
		return UpdateDatabaseResponse.fail(database.getRepoId(), new ExecutionControl.NotImplementedException("Method not implemented yet"));
	}
	
}
