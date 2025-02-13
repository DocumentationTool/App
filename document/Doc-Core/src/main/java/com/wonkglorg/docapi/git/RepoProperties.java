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
		private boolean readOnly = false;
		/**
		 * Name of the database file
		 */
		private String dbName = "data.sql";

	/**
	 * defines in what repo the data.sql is stored in (if the repo is marked as readOnly
	 */
	private Path dbStorage = path;

		
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

	public Path getDbStorage() {
		return dbStorage;
	}

	public void setDbStorage(Path dbStorage) {
		this.dbStorage = dbStorage;
	}


}