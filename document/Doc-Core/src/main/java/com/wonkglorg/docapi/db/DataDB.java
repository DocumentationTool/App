package com.wonkglorg.docapi.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataDB extends SqliteDatabase{
	private static final Logger log = LoggerFactory.getLogger(DataDB.class);
	
	public DataDB(Path sourcePath, Path destinationPath) {
		super(sourcePath, destinationPath);
	}
	
	public DataDB(Path openInPath) {
		super(openInPath);
	}
	
	public void initialize() {
		log.info("Initialising Database");
		try{
			connection.setAutoCommit(false);
			executeStatement("""
					CREATE TABLE Roles (
					    roleID TEXT PRIMARY KEY,
					    roleName TEXT
					);
					""");
			executeStatement("""
					CREATE TABLE Users (
					    userID TEXT PRIMARY KEY,
					    created_at TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP,
					    last_modified_by TEXT
					);
					""");
			executeStatement("""
					CREATE TABLE UserRoles (
					    roleID TEXT,
					    userID TEXT,
					    PRIMARY KEY (roleID, userID),
					    FOREIGN KEY (roleID) REFERENCES Roles(roleID),
					    FOREIGN KEY (userID) REFERENCES Users(userID)
					);
					""");
			executeStatement("""
					CREATE TABLE Groups (
					    groupID TEXT PRIMARY KEY,
					    group_name TEXT,
					    created_at TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP,
					    last_modified_by TEXT
					);
					""");
			executeStatement("""
					CREATE TABLE GroupUsers (
					    userID TEXT,
					    groupID TEXT,
					    PRIMARY KEY (userID, groupID),
					    FOREIGN KEY (userID) REFERENCES Users(userID),
					    FOREIGN KEY (groupID) REFERENCES Groups(groupID)
					);
					""");
			
			executeStatement("""
					CREATE TABLE Resource (
					    resourcePath TEXT PRIMARY KEY,
					    created_at TIMESTAMP,
					    created_by TEXT,
					    last_modified_at TIMESTAMP,
					    last_modified_by TEXT
					);
					""");
			
			executeStatement("""
					CREATE TABLE GroupPermissions (
					    groupID TEXT,
					    path TEXT,
					    type TEXT,
					    PRIMARY KEY (groupID, path),
					    FOREIGN KEY (groupID) REFERENCES Groups(groupID),
					    FOREIGN KEY (path) REFERENCES Resource(resourcePath)
					);
					""");
			
			executeStatement("""
					CREATE TABLE UserPermissions (
					    userID TEXT,
					    path TEXT,
					    type TEXT,
					    PRIMARY KEY (userID, path),
					    FOREIGN KEY (userID) REFERENCES Users(userID),
					    FOREIGN KEY (path) REFERENCES Resource(resourcePath)
					);
					""");
			
			executeStatement("""
					CREATE TABLE AuditLog (
					    logID INTEGER PRIMARY KEY,
					    userID TEXT,
					    action TEXT, -- Action type, e.g., 'grant', 'revoke'
					    permissionID TEXT,
					    timestamp TIMESTAMP,
					    affected_userID TEXT,
					    affected_groupID TEXT,
					    FOREIGN KEY (userID) REFERENCES Users(userID)
					);
					""");
			
			executeStatement("""
					CREATE TABLE Permissions (
					    permissionID TEXT PRIMARY KEY,
					    weight INTEGER
					);
					""");
			
			executeStatement("ALTER TABLE UserPermissions ADD FOREIGN KEY (type) REFERENCES Permissions(permissionID);");
			executeStatement("ALTER TABLE GroupPermissions ADD FOREIGN KEY (type) REFERENCES Permissions(permissionID);");
			
			connection.commit();
		} catch(SQLException e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				throw new RuntimeException(ex);
			}
		} finally{
			try{
				connection.setAutoCommit(true);
			} catch(SQLException e){
				throw new RuntimeException(e);
			}
		}
	}
	
	private void executeStatement(String sql) throws SQLException {
		try(PreparedStatement statement = connection.prepareStatement(sql)){
			statement.execute();
		}
		
	}
	
}
