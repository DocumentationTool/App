package com.wonkglorg.docapi.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DataDB extends SqliteDatabase {
	private static final Logger log = LoggerFactory.getLogger(DataDB.class);

	public DataDB(Path sourcePath, Path destinationPath) {
		super(sourcePath, destinationPath);
	}

	public DataDB(Path openInPath) {
		super(openInPath);
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
	public Set<Path> getResources() {
		Set<Path> resources = new HashSet<>();
		try (var statement = getConnection().prepareStatement("SELECT resourcePath FROM Resources")) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				resources.add(Path.of(resultSet.getString(1)));
			}
			return resources;
		} catch (SQLException e) {
			log.error("Error while reading resources", e);
			return resources;
		}
	}

	public void initialize() {
		log.info("Initialising Database");
		try {
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
					CREATE TABLE IF NOT EXISTS Resources (
					                          resourcePath TEXT PRIMARY KEY,
					                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          created_by TEXT,
					                          last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
					                          last_modified_by TEXT
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
			log.error("Error while initialising Database", e);
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
		log.info("Inserting resource {}", path);
		try (var statement = getConnection().prepareStatement(
				"INSERT INTO Resources(resourcePath, created_at, created_by, last_modified_at, "
						+ "last_modified_by) VALUES(?,datetime('now'),'system',datetime('now'),'system')")) {
			statement.setString(1, path.toString());
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			log.error("Error while inserting resource", e);
			return false;
		}
	}

	public boolean removeResource(Path path) {
		log.info("Deleting resource {}", path);
		try (var statement = getConnection().prepareStatement(
				"DELETE FROM Resources WHERE resourcePath = ?")) {
			statement.setString(1, path.toString());
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			log.error("Error while removing resource", e);
			return false;
		}
	}

	public boolean updateResources(Set<Path> files) {
		boolean filesChanged = false;
		log.info("Updating resources");
		Set<Path> existingFiles = getResources();

		Set<Path> filesToAdd = new HashSet<>(files);
		filesToAdd.removeAll(existingFiles);

		Set<Path> filesToRemove = new HashSet<>(existingFiles);
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
		return filesChanged;
	}

}
