package com.wonkglorg.docapi.git;

import java.nio.file.Path;

/**
	 * Represents a single repository that is being managed by the application
	 */
	public class RepoProperties {
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