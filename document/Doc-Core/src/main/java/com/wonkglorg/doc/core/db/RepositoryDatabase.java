package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.builder.statement.Batch;
import com.wonkglorg.doc.core.db.builder.statement.Update;
import com.wonkglorg.doc.core.db.daos.DatabaseFunctions;
import com.wonkglorg.doc.core.db.daos.ResourceFunctions;
import com.wonkglorg.doc.core.db.daos.UserFunctions;
import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents the database object for a defined repository
 */
public class RepositoryDatabase extends SqliteDatabase<HikariDataSource> implements ResourceFunctions, UserFunctions{
	private static final Logger log = LoggerFactory.getLogger(RepositoryDatabase.class);
	private final RepoProperty repoProperties;
	
	public RepositoryDatabase(RepoProperty repoProperties, Path openInPath) {
		super(getDataSource(openInPath));
		this.repoProperties = repoProperties;
	}
	
	public RepositoryDatabase(RepoProperty repoProperties) {
		this(repoProperties, repoProperties.getPath().resolve(repoProperties.getDbName()));
	}
	
	/**
	 * Retrieves the data source for the current sql connection
	 *
	 * @param openInPath the path to open the data source in
	 * @return the created data source
	 */
	private static HikariDataSource getDataSource(Path openInPath) {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
		return new HikariDataSource(hikariConfig);
	}
	
	/**
	 * Initializes the database for the current repo (creating tables, triggers, etc.)
	 */
	@SuppressWarnings("TextBlockBackwardMigration")
	public void initialize() {
		log.info("Initialising Database for repo '{}'", repoProperties.getId());
		try{
			DatabaseFunctions.initializeDatabase(getConnection());
			log.info("Creating triggers");
			DatabaseFunctions.setupUpdateResourceTrigger(getConnection());
			DatabaseFunctions.setupRemoveResourceTriggers(getConnection());
		} catch(Exception e){
			log.error("Error while initializing Database for repo '{}'", repoProperties.getId(), e);
		}
		
		log.info("Database initialized for repo '{}'", repoProperties.getId());
	}
	
	/**
	 * Rebuilds the entire ntfs table to remove any unused records
	 */
	public void rebuildFts() {
		log.info("Rebuilding FTS for repo '{}'", repoProperties.getId());
		try{
			voidAttach(DatabaseFunctionsold.class, DatabaseFunctionsold::rebuildFts);
			log.info("Finished rebuilding FTS for repo '{}'", repoProperties.getId());
		} catch(Exception e){
			log.error("Error while rebuilding FTS for repo '{}'", repoProperties.getId(), e);
		}
	}
	
	@Override
	public int deleteResource(Path resourcePath) {
		log.info("Deleting resource {} for repo {}", resourcePath, repoProperties.getId());
		try{
			return attach(ResourceFunctions.class, r -> r.deleteResource(resourcePath));
		} catch(Exception e){
			log.error("Error while deleting {} from repo {}", resourcePath, repoProperties.getId(), e);
			return -1;
		}
	}
	
	@Override
	public int deleteData(Path resourcePath) {
		log.info("Deleting data {} for repo {}", resourcePath, repoProperties.getId());
		try{
			return attach(ResourceFunctions.class, r -> r.deleteData(resourcePath));
		} catch(Exception e){
			log.error("Error while deleting data {} from repo {}", resourcePath, repoProperties.getId(), e);
			return -1;
		}
	}
	
	@Override
	public List<Resource> getResources() throws RuntimeSQLException {
		log.info("Retrieving resources for repo {}", repoProperties.getId());
		try{
			return attach(ResourceFunctions.class, ResourceFunctions::getResources);
		} catch(Exception e){
			log.error("Error while retrieving resources from {}", repoProperties.getId());
			throw new RuntimeSQLException(e);
		}
	}
	
	@Override
	public Resource findByPath(Path resourcePath) {
		return null;
	}
	
	@Override
	public List<Resource> findByContent(String searchTerm) {
		return List.of();
	}
	
	@Override
	public void insertResource(Resource resource) throws RuntimeSQLException {
		log.info("Inserting resource {} for repo {}", resource, repoProperties.getId());
		try{
			voidAttach(ResourceFunctions.class, r -> r.insertResource(resource));
		} catch(Exception e){
			log.error("Error while inserting {} from repo {}", resource, repoProperties.getId(), e);
			throw new RuntimeSQLException(e);
		}
	}
	
	public void insertResourceOld(Resource resource) throws RuntimeSQLException {
		log.info("Inserting resource {} for repo {}", resource, repoProperties.getId());
		try(Handle handle = jdbi().open(); Update update = handle.createUpdate("""
				 INSERT INTO Resources(resource_path,created_at,created_by,last_modified_at,last_modified_by,commit_id)
				    VALUES(:resourcePath,:createdAt,:createdBy,:lastModifiedAt,:lastModifiedBy,:commitId);
				                    INSERT INTO FileData(resource_path,data) VALUES(:resourcePath,:data);
				""")){
			update.bind("resourcePath", resource.resourcePath());
			update.bind("data", resource.data());
			update.bind("createdAt", resource.createdAt());
			update.bind("createdBy", resource.createdBy());
			update.bind("commitId", resource.commitId());
			update.bind("modifiedBy", resource.modifiedBy());
			update.bind("modifiedAt", resource.modifiedAt());
			update.execute();
			
		} catch(Exception e){
			log.error("Error while inserting {} from repo {}", resource, repoProperties.getId(), e);
		}
	}
	
	@Override
	public int updatePath(Path oldPath, Path newPath) {
		log.info("Moving resource '{}' to '{}' in repo '{}'", oldPath, newPath, repoProperties.getId());
		try{
			return attach(ResourceFunctions.class, r -> r.updatePath(oldPath, newPath));
		} catch(Exception e){
			log.error("Error while moving resource '{}' in repo '{}'", oldPath, repoProperties.getId(), e);
			return -1;
		}
	}
	
	@Override
	public int updateResource(Path resourcePath, String newData) {
		log.info("Updating resource '{}' in repo '{}'", resourcePath, repoProperties.getId());
		try{
			return attach(ResourceFunctions.class, db -> db.updateResource(resourcePath, newData));
		} catch(Exception e){
			log.error("Error while updating resource '{}' in repo '{}'", resourcePath, repoProperties.getId(), e);
			return -1;
		}
	}
	
	public void batchInsert(List<Map.Entry<Resource, String>> resources) {
		try(Handle handle = jdbi().open(); Batch batch = handle.createBatch()){
			for(var resourceEntry : resources){
				var resource = resourceEntry.getKey();
				var data = resourceEntry.getValue();
				String sql = "INSERT INTO FileData(resourcePath, data) VALUES(%s,%s)".formatted(resource.resourcePath().toString(), data);
				batch.add(sql);
			}
			batch.execute();
		}
	}
	
	/*
	public boolean updateResources(Set<Path> files) {
		todo:jmd compare last changes + other info to determin if changes happened, if so rebuild
		that particular entry and redo.
		boolean filesChanged = false;

		log.info("Updating resources for '{}'.", repoProperties.getId());
		Set<Path> existingFiles = getResources().stream().map(Resource::resourcePath).collect(Collectors.toSet());
		Set<Path> modifiedFiles = new HashSet<>(existingFiles);
		modifiedFiles.forEach(path -> {
		});

		Set<Path> filesToAdd = new HashSet<>(files);
		filesToAdd.removeAll(existingFiles);

		Set<Path> filesToRemove = new HashSet<>(existingFiles);
		filesToRemove.removeAll(files);

		if (!filesToRemove.isEmpty() || !filesToAdd.isEmpty()) {
			filesChanged = true;
		}
		try (Handle handle = jdbi().open()) {
			ResourceFunctions resources = handle.attach(ResourceFunctions.class);
			for (Path path : filesToRemove) {
				//resources.deleteResource(path);
			}
		}



		//todo:jmd get the commit id
		for (Path path : filesToAdd) {
			//insertResource(path, "default");
		}
		log.info("Finished updating resources for '{}'.", repoProperties.getId());
		log.info("Added: {}", filesToAdd.size());
		log.info("Modified: {}", filesChanged);
		log.info("Deleted: {}", filesToRemove.size());
		return filesChanged;
	}
	 */
	@Override
	public int addUser(UserId userId, String password, String createdBy) {
		log.info("Adding user '{}' in repo '{}'", userId, repoProperties.getId());
		if(this.getUser(userId) != null){
			log.info("Unable to add new user {}, already exists in repo '{}'", userId, repoProperties.getId());
			return -1;
		}
		try{
			Integer i = attach(UserFunctions.class, r -> r.addUser(userId, password, createdBy));
			log.info("Added new user {} in repo '{}'", userId, repoProperties.getId());
			return i;
		} catch(Exception e){
			log.error("Error while adding user {} in repo '{}'", userId, repoProperties.getId(), e);
			return -1;
		}
	}
	
	@Override
	public List<UserId> getUsersFromGroup(GroupId groupId) {
		log.info("Getting users from group '{}' in repO '{}'.", groupId, repoProperties.getId());
		try{
			return attach(UserFunctions.class, db -> db.getUsersFromGroup(groupId));
		} catch(Exception e){
			log.error("Error while getting group '{}' in repo '{}'", groupId, repoProperties.getId(), e);
			return List.of();
		}
	}
	
	@Override
	public List<GroupId> getGroupsFromUser(UserId userId) {
		log.info("Getting groups from user '{}' in repo '{}'.", userId, repoProperties.getId());
		try{
			return attach(UserFunctions.class, db -> db.getGroupsFromUser(userId));
		} catch(Exception e){
			log.error("Error while getting group '{}' in repo '{}'", userId, repoProperties.getId(), e);
			return List.of();
		}
	}
	
	@Override
	public UserProfile getUser(UserId userId) {
		log.info("Finding user '{}' in repo '{}'.", userId, repoProperties.getId());
		try{
			return attach(UserFunctions.class, db -> db.getUser(userId));
		} catch(Exception e){
			log.error("Error while Finding user '{}' in repo '{}'", userId, repoProperties.getId(), e);
			return null;
		}
	}
	
	public RepoProperty getRepoProperties() {
		return repoProperties;
	}
}
