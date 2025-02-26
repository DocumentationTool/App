package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.daos.DatabaseFunctions;
import com.wonkglorg.doc.core.db.daos.ResourceFunctions;
import com.wonkglorg.doc.core.db.daos.UserFunctions;
import com.wonkglorg.doc.core.db.dbs.JdbiDatabase;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the database object for a defined repository
 */
public class RepositoryDatabase extends JdbiDatabase<HikariDataSource> implements UserFunctions, ResourceFunctions{
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
	 * @param repoProperties
	 * @param openInPath
	 * @return
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
		log.info("Initialising Database for repo '{}'", repoProperties.getName());
		try{
			voidAttach(com.wonkglorg.docapi.db.daos.DatabaseFunctions.class, com.wonkglorg.docapi.db.daos.DatabaseFunctions::initialize);
			log.info("Creating triggers");
			voidAttach(com.wonkglorg.docapi.db.daos.DatabaseFunctions.class, com.wonkglorg.docapi.db.daos.DatabaseFunctions::setupTriggers);
		} catch(Exception e){
			log.error("Error while initializing Database for repo '{}'", repoProperties.getName(), e);
		}
		
		log.info("Database initialized for repo '{}'", repoProperties.getName());
	}
	
	/**
	 * Rebuilds the entire ntfs table to remove any unused records
	 */
	public void rebuildFts() {
		log.info("Rebuilding FTS for repo '{}'", repoProperties.getName());
		
		try{
			voidAttach(DatabaseFunctions.class, DatabaseFunctions::rebuildFts);
			log.info("Finished rebuilding FTS for repo '{}'", repoProperties.getName());
		} catch(Exception e){
			log.error("Error while rebuilding FTS for repo '{}'", repoProperties.getName(), e);
		}
	}
	
	@Override
	public void deleteResource(Path resourcePath) {
		log.info("Deleting resource {} for repo {}", resourcePath, repoProperties.getName());
		try{
			voidAttach(ResourceFunctions.class, r -> r.deleteResource(resourcePath));
		} catch(RuntimeException e){
			log.error("Error while deleting {} from repo {}", resourcePath, repoProperties.getName(), e);
		}
	}
	
	@Override
	public void deleteData(Path resourcePath) {
	
	}
	
	@Override
	public List<Resource> getResources() {
		try{
			attach(ResourceFunctions.class, ResourceFunctions::getResources);
		} catch(Exception e){
			log.error("Error while retrieving resources from {}", repoProperties.getName());
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
	public int insert(Resource resource) {
		return 0;
	}
	
	@Override
	public int updatePath(Path oldPath, Path newPath) {
		log.info("Moving resource '{}' to '{}' in repo '{}'", oldPath, newPath, repoProperties.getName());
		try{
			attach(ResourceFunctions.class, r -> r.updatePath(oldPath, newPath));
		} catch(Exception e){
			log.error("Error while moving resource '{}' in repo '{}'", oldPath, repoProperties.getName(), e);
		}
	}
	
	public boolean updateResource(Path resourcePath, String newData) {
		log.info("Updating resource '{}' in repo '{}'", resourcePath, repoProperties.getName());
		try{
			return attach(ResourceFunctions.class, db -> db.updateResource(resourcePath, newData)) > 0;
		} catch(Exception e){
			log.error("Error while updating resource '{}' in repo '{}'", resourcePath, repoProperties.getName(), e);
		}
		return false;
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
	
	public boolean updateResources(Set<Path> files) {
		//todo:jmd compare last changes + other info to determin if changes happened, if so rebuild
		// that particular entry and redo.
		boolean filesChanged = false;
		
		log.info("Updating resources for '{}'.", repoProperties.getName());
		Set<Path> existingFiles = getResources().stream().map(Resource::resourcePath).collect(Collectors.toSet());
		Set<Path> modifiedFiles = new HashSet<>(existingFiles);
		modifiedFiles.forEach(path -> {
		});
		
		Set<Path> filesToAdd = new HashSet<>(files);
		filesToAdd.removeAll(existingFiles);
		
		Set<Path> filesToRemove = new HashSet<>(existingFiles);
		filesToRemove.removeAll(files);
		
		if(!filesToRemove.isEmpty() || !filesToAdd.isEmpty()){
			filesChanged = true;
		}
		try(Handle handle = jdbi().open()){
			ResourceFunctions resources = handle.attach(ResourceFunctions.class);
			for(Path path : filesToRemove){
				resources.deleteResource(path);
			}
		}
		
		//todo:jmd get the commit id
		for(Path path : filesToAdd){
			insertResource(path, "default");
		}
		log.info("Finished updating resources for '{}'.", repoProperties.getName());
		log.info("Added: {}", filesToAdd.size());
		log.info("Modified: {}", filesChanged);
		log.info("Deleted: {}", filesToRemove.size());
		return filesChanged;
	}
	
	@Override
	public int addUserFunction(UserId userId, String password, String createdBy) {
		return 0;
	}
	
	@Override
	public List<UserId> getUsersFromGroup(GroupId groupId) {
		return attach(UserFunctions.class, db -> db.getUsersFromGroup(groupId));
	}
	
	@Override
	public List<GroupId> getGroupsFromUser(UserId userId) {
		return attach(UserFunctions.class, db -> db.getGroupsFromUser(userId));
	}
	
}
