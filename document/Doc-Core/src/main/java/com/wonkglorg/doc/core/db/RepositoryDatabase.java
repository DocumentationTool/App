package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.db.functions.DatabaseFunctions;
import com.wonkglorg.doc.core.db.functions.PermissionFunctions;
import com.wonkglorg.doc.core.db.functions.ResourceFunctions;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Represents the database object for a defined repository
 */
@SuppressWarnings("UnusedReturnValue")
public class RepositoryDatabase extends SqliteDatabase<HikariDataSource>{
	
	private static final Logger log = LoggerFactory.getLogger(RepositoryDatabase.class);
	private final PermissionFunctions permissionFunctions;
	private final ResourceFunctions resourceFunctions;
	private final FileRepository fileRepository;
	/**
	 * The properties of the repository
	 */
	private final RepoProperty repoProperties;
	
	public RepositoryDatabase(RepoProperty repoProperties, Path openInPath, FileRepository fileRepository) {
		super(getDataSource(openInPath));
		this.fileRepository = fileRepository;
		this.repoProperties = repoProperties;
		this.resourceFunctions = new ResourceFunctions(this);
		this.permissionFunctions = new PermissionFunctions(this);
	}
	
	public RepositoryDatabase(RepoProperty repoProperties, FileRepository fileRepository) {
		this(repoProperties, repoProperties.getPath().resolve(repoProperties.getDbName()), fileRepository);
	}
	
	/**
	 * Retrieves the data source for the current sql connection
	 *
	 * @param openInPath the path to open the data source in
	 * @return the created data source
	 */
	private static HikariDataSource getDataSource(Path openInPath) {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setLeakDetectionThreshold(1000);
		hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
		return new HikariDataSource(hikariConfig);
	}
	
	/**
	 * Initializes the database for the current repo (creating tables, triggers, etc.)
	 */
	public void initialize() throws CoreSqlException {
		log.info("Initialising Database for repo '{}'", repoProperties.getId());
		try{
			DatabaseFunctions.initializeDatabase(this);
			log.info("Creating triggers");
			DatabaseFunctions.initializeTriggers(this);
			//todo:jmd add more triggers for users
		} catch(RuntimeException e){
			log.error("Error while initializing Database for repo '{}'", repoProperties.getId(), e);
		}
		log.info("Database initialized for repo '{}'", repoProperties.getId());
		resourceFunctions.initialize();
		permissionFunctions.initialize();
	}
	
	/**
	 * Rebuilds the entire FTS table to remove any unused records
	 */
	public void rebuildFts() throws CoreSqlException {
		DatabaseFunctions.rebuildFts(this);
	}
	
	public RepoId getRepoId() {
		return repoProperties.getId();
	}
	
	public RepoProperty getRepoProperties() {
		return repoProperties;
	}
	
	public ResourceFunctions resourceFunctions() {
		return resourceFunctions;
	}
	
	public PermissionFunctions permissionFunctions() {
		return permissionFunctions;
	}
	
	public FileRepository getFileRepository() {
		return fileRepository;
	}
}
