package com.wonkglorg.docapi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("doc.git")
public class RepoProperties{
	/**
	 * List of repositories that are being managed by the application
	 */
	private final List<RepoProperty> repositories = new ArrayList<>();
	
	public List<RepoProperty> getRepositories() {
		return repositories;
	}
	
	/**
	 * Represents a single repository that is being managed by the application
	 */
	public static class RepoProperty{
		/**
		 * Name to visually display the repository with
		 */
		private String name = "Repository";
		/**
		 * Path to the repository
		 */
		private Path path;
		/**
		 * If the repository is read only
		 */
		private boolean readOnly = true;
		/**
		 * Name of the database file
		 */
		private String dbName = "data.sql";
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Path getPath() {
			return path;
		}
		
		public void setPath(Path path) {
			this.path = path;
		}
		
		public boolean isReadOnly() {
			return readOnly;
		}
		
		public void setReadOnly(boolean readOnly) {
			this.readOnly = readOnly;
		}
		
		public String getDbName() {
			return dbName;
		}
		
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
	}
	
}
