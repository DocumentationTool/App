package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.permissions.PermissionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
     * Initializes the database with the required tables and constraints
     */
    public static void initializeDatabase(RepositoryDatabase database) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            statement.execute("PRAGMA auto_vacuum = INCREMENTAL");
            statement.execute("PRAGMA incremental_vacuum(500)");

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
                    CREATE TABLE IF NOT EXISTS GroupPermissions(
                        group_id TEXT NOT NULL,
                        path TEXT NOT NULL,
                        type TEXT NOT NULL,
                        PRIMARY KEY (group_id, path)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS UserPermissions(
                        user_id TEXT NOT NULL,
                        path TEXT NOT NULL,
                        type TEXT NOT NULL,
                        PRIMARY KEY (user_id, path)
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

            //admin account gets all permissions by default
            statement.execute("""
                    INSERT OR IGNORE INTO UserPermissions(user_id, path,type) VALUES ('admin','**','%s');
                    """.formatted(PermissionType.ADMIN));

            statement.execute("PRAGMA foreign_keys = ON");
        } catch (Exception e) {
            throw new CoreSqlException("Error while initializing the database in '%s'".formatted(database.getRepoId()), e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Rebuilds the FTS table when called, this is a slow operation and should only be done when there is a specific need to do so
     */
    public static void rebuildFts(RepositoryDatabase database) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (Statement statement = connection.createStatement()) {
            //noinspection SqlResolve on purpose sql plugin doesn't recognize the fts specific commands
            statement.executeUpdate(("INSERT INTO FileData(FileData) VALUES ('rebuild')"));
        } catch (Exception e) {
            throw new CoreSqlException("Error while rebuilding FTS in '%s'".formatted(database.getRepoId()), e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Creates a trigger that deletes all accompanying tables resources when the main "Resources" table gets deleted
     */
    public static void initializeTriggers(RepositoryDatabase database) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TRIGGER IF NOT EXISTS update_resource_path
                    AFTER UPDATE ON Resources
                    FOR EACH ROW
                    WHEN OLD.resource_path != NEW.resource_path
                    BEGIN
                        -- Update related permissions
                        UPDATE GroupPermissions SET path = NEW.resource_path WHERE path = OLD.resource_path;
                        UPDATE UserPermissions SET path = NEW.resource_path WHERE path = OLD.resource_path;
                        -- Update related tags
                        UPDATE ResourceTags SET resource_path = NEW.resource_path WHERE resource_path = OLD.resource_path;
                        -- Update indexed data
                        UPDATE FileData SET resource_path = NEW.resource_path WHERE resource_path = OLD.resource_path;
                    END;
                    """);

            statement.execute("""
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
                    """);

            statement.execute("""
                    CREATE TRIGGER IF NOT EXISTS delete_tag_cleanup
                    AFTER DELETE ON Tags
                    FOR EACH ROW
                    BEGIN
                       -- Delete related tagid
                        DELETE FROM ResourceTags WHERE tag_id = OLD.tag_id;
                    END;
                    """);
        } catch (Exception e) {
            throw new CoreSqlException("Error while initializing the database", e);
        } finally {
            closeConnection(connection);
        }
    }

    private static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Error while closing connection", e);
        }
    }
}
