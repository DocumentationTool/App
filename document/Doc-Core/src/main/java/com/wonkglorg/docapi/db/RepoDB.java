package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.git.RepoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.wonkglorg.docapi.db.DbObjects.*;

public class RepoDB extends SqliteDatabase {
	private static final Logger log = LoggerFactory.getLogger(RepoDB.class);
	private final RepoProperties repoProperties;

	public RepoDB(RepoProperties repoProperties, Path sourcePath, Path destinationPath) {
		super(sourcePath, destinationPath);
		this.repoProperties = repoProperties;
	}

	public RepoDB(RepoProperties repoProperties, Path openInPath) {
		super(openInPath);
		this.repoProperties = repoProperties;
	}

	private void executeStatement(String sql) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.execute();
		}

	}

	/**
	 * Retrieve all saved resources from the database
	 *
	 * @return all resources or an empty set
	 */
	public Set<Resource> getResources() {
		Set<Resource> resources = new HashSet<>();
		try (var statement = getConnection().prepareStatement("SELECT resourcePath,created_at,created_by,last_modified_at,last_modified_by FROM Resources")) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				Path path = Path.of(resultSet.getString(1));
				LocalDateTime createdAt = LocalDateTime.parse(resultSet.getString(2));
				String createdBy = resultSet.getString(3);
				LocalDateTime lastModifiedAt = LocalDateTime.parse(resultSet.getString(3));
				String lastModifiedBy = resultSet.getString(4);
				resources.add(new Resource(path, createdAt, createdBy, lastModifiedAt, lastModifiedBy));
			}
			return resources;
		} catch (SQLException e) {
			log.error("Error while reading resources for repo '{}'", repoProperties.getName(), e);
			return resources;
		}
	}

	public void initialize() {
		log.info("Initialising Database for repo '{}'", repoProperties.getName());
		try {
			executeStatement("PRAGMA auto_vacuum = INCREMENTAL;");
			executeStatement("PRAGMA incremental_vacuum(500);");
			executeStatement("PRAGMA foreign_keys = OFF;");
			connection.setAutoCommit(false);
			executeStatement("""
					CREATE TABLE IF NOT EXISTS Roles (
					                       roleID TEXT PRIMARY KEY,
					                       roleName TEXT NOT NULL
					);
					""");
			executeStatement("""
					CREATE TABLE IF NOT EXISTS Users (
					                       userID TEXT PRIMARY KEY,
					                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                       created_by TEXT,
					                       last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                       last_modified_by TEXT
					);
					""");
			executeStatement("""
					CREATE TABLE IF NOT EXISTS UserRoles (
					                           roleID TEXT,
					                           userID TEXT,
					                           PRIMARY KEY (roleID, userID),
					                           FOREIGN KEY (roleID) REFERENCES Roles(roleID),
					                           FOREIGN KEY (userID) REFERENCES Users(userID)
					);
					""");
			executeStatement("""
					CREATE TABLE IF NOT EXISTS Groups (
					                        groupID TEXT PRIMARY KEY,
					                        group_name TEXT NOT NULL,
					                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                        created_by TEXT,
					                        last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                        last_modified_by TEXT
					);
					""");
			executeStatement("""
					CREATE TABLE IF NOT EXISTS GroupUsers (
					                            userID TEXT,
					                            groupID TEXT,
					                            PRIMARY KEY (userID, groupID),
					                            FOREIGN KEY (userID) REFERENCES Users(userID),
					                            FOREIGN KEY (groupID) REFERENCES Groups(groupID)
					);
					""");

			executeStatement("""
					CREATE TABLE IF NOT EXISTS Tags(
					                          tag TEXT PRIMARY KEY,
					                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          created_by TEXT
					);
					""");


			executeStatement("""
					CREATE TABLE IF NOT EXISTS Resources (
					                          resourcePath TEXT PRIMARY KEY,
					                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          created_by TEXT,
					                          last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          last_modified_by TEXT
					);
					""");

			executeStatement("""
					CREATE TABLE IF NOT EXISTS ResourceTags(
					                          tag TEXT,
					                          resourcePath TEXT,
					                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          created_by TEXT,
					                          PRIMARY KEY (tag, resourcePath),
					                          FOREIGN KEY (tag) REFERENCES Tags(tag),
					                          FOREIGN KEY (resourcePath) REFERENCES Resources(resourcePath)
					);
					""");

			executeStatement("""
					CREATE TABLE IF NOT EXISTS GroupPermissions (
					                                  groupID TEXT,
					                                  path TEXT,
					                                  type TEXT,
					                                  PRIMARY KEY (groupID, path),
					                                  FOREIGN KEY (groupID) REFERENCES Groups(groupID),
					                                  FOREIGN KEY (path) REFERENCES Resources(resourcePath),
					                                  FOREIGN KEY (type) REFERENCES Permissions(permissionID)
					);
					""");

			executeStatement("""
					CREATE TABLE IF NOT EXISTS UserPermissions (
					                                 userID TEXT,
					                                 path TEXT,
					                                 type TEXT,
					                                 PRIMARY KEY (userID, path),
					                                 FOREIGN KEY (userID) REFERENCES Users(userID),
					                                 FOREIGN KEY (path) REFERENCES Resources(resourcePath),
					                                 FOREIGN KEY (type) REFERENCES Permissions(permissionID)
					);
					""");

			executeStatement("""
					CREATE TABLE IF NOT EXISTS Permissions (
					                             permissionID TEXT PRIMARY KEY,
					                             weight INTEGER NOT NULL
					);
					""");

			executeStatement("""
					CREATE TABLE IF NOT EXISTS AuditLog (
					                          logID INTEGER PRIMARY KEY AUTOINCREMENT,
					                          userID TEXT,
					                          action TEXT NOT NULL,  -- Action type, e.g., 'grant', 'revoke'
					                          permissionID TEXT,
					                          timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          affected_userID TEXT,
					                          affected_groupID TEXT,
					                          FOREIGN KEY (userID) REFERENCES Users(userID),
					                          FOREIGN KEY (permissionID) REFERENCES Permissions(permissionID),
					                          FOREIGN KEY (affected_userID) REFERENCES Users(userID),
					                          FOREIGN KEY (affected_groupID) REFERENCES Groups(groupID)
					);
					""");

			executeStatement("PRAGMA foreign_keys = ON;");
			connection.commit();
		} catch (SQLException e) {
			log.error("Error while initialising Database for repo '{}'", repoProperties.getName(), e);
			try {
				connection.rollback();
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Inserts a new resource into the database
	 *
	 * @param path the path the resource is located at
	 * @return true if it was inserted false otherwise
	 */
	public boolean insertResource(Path path) {
		log.info("Inserting resource '{}' into repo '{}'", path, repoProperties.getName());
		try (var statement = getConnection().prepareStatement(
				"INSERT INTO Resources(resourcePath, created_at, created_by, last_modified_at, "
						+ "last_modified_by) VALUES(?,datetime('now'),'system',datetime('now'),'system')")) {
			statement.setString(1, path.toString());
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			log.error("Error while inserting resource '{}' from repo '{}'", path,
					repoProperties.getName(), e);
			return false;
		}
	}

	/**
	 * Rebuilds the entire ntfs table to remove any unused records
	 */
	@SuppressWarnings("SqlResolve")//disabled duo to wrong errors showing up duo to fts specific command
	public void rebuildFts() {
		log.info("Rebuilding FTS for repo '{}'", repoProperties.getName());
		try (var statement = getConnection().prepareStatement(
				"INSERT INTO renderedPages(renderedPages) VALUES ('rebuild')")) { //rebuilds the table to reduce any old data
			statement.execute();
			log.info("Finished rebuilding FTS for repo '{}'", repoProperties.getName());
		} catch (SQLException e) {
			log.error("Error while rebuilding FTS for repo '{}'", repoProperties.getName(), e);
		}
	}


	public boolean removeResource(Path path) {
		log.info("Deleting resource '{}' from repo '{}'", path, repoProperties.getName());
		try (var statement = getConnection().prepareStatement(
				"DELETE FROM Resources WHERE resourcePath = ?")) {
			statement.setString(1, path.toString());

			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			log.error("Error while removing resource '{}' from repo '{}'", path,
					repoProperties.getName(),
					e);
			return false;
		}
	}

	public boolean updateResources(Set<Path> files) {
		boolean filesChanged = false;

		log.info("Updating resources for '{}'.", repoProperties.getName());
		Set<Resource> existingFiles = getResources();
		Set<Resource> modifiedFiles = new HashSet<>(existingFiles);
		modifiedFiles.forEach(path -> {});

		Set<Resource> filesToAdd = new HashSet<>(files);
		filesToAdd.removeAll(existingFiles);

		Set<Resource> filesToRemove = new HashSet<>(existingFiles);
		filesToRemove.removeAll(files);


		if (!filesToRemove.isEmpty() || !filesToAdd.isEmpty()) {
			filesChanged = true;
		}

		for (Path path : filesToRemove) {
			removeResource(path);
		}

		for (Path path : filesToAdd) {
			insertResource(path);
		}
		log.info("Finished updating resources for '{}'.", repoProperties.getName());
		log.info("Added: {}", filesToAdd.size());
		log.info("Modified: {}", filesChanged);
		log.info("Deleted: {}", filesToRemove.size());
		return filesChanged;
	}



}
