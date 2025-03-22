package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.DbHelper;
import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.interfaces.ResourceCalls;
import com.wonkglorg.doc.core.objects.DateHelper;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All resource related database functions
 */
public class ResourceFunctions implements IDBFunctions, ResourceCalls{
	
	/**
	 * The path matcher for this database
	 */
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();
	private static final Logger log = LoggerFactory.getLogger(ResourceFunctions.class);
	private final RepositoryDatabase database;
	
	/**
	 * The cache of resources for this database
	 */
	private final Map<Path, Resource> resourceCache = new java.util.concurrent.ConcurrentHashMap<>();
	/**
	 * The cache of tags for this database
	 */
	private final Map<TagId, Tag> tagCache = new HashMap<>();
	
	/**
	 * Keeps track of currently edited files todo add a way to every now and then confirm if its still edited
	 */
	private final Map<UserId, Path> currentlyEdited = new HashMap<>();
	
	public ResourceFunctions(RepositoryDatabase database) {
		this.database = database;
	}
	
	@Override
	public void initialize() {
		Connection connection = database.getConnection();
		try{
			List<Resource> resources = getAllResources(connection);
			for(Resource resource : resources){
				resourceCache.put(resource.resourcePath(), resource);
			}
			
			var allTags = getAllTags(connection);
			for(Tag tag : allTags){
				tagCache.put(tag.tagId(), tag);
			}
		} catch(CoreSqlException e){
			log.error("Failed to initialize resource functions", e);
		} finally{
			closeConnection(connection);
		}
		
	}
	
	/**
	 * Retrieves a list of all resources contained in the given repository databases table(without its content attached)
	 */
	private List<Resource> getAllResources(Connection connection) throws CoreSqlException {
		List<Resource> resources = new ArrayList<>();
		String query = "SELECT resource_path, created_at, created_by, last_modified_at, last_modified_by, category " + "FROM Resources";
		try(PreparedStatement statement = connection.prepareStatement(query)){
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				resources.add(resourceFromResultSet(resultSet, new HashSet<>(), null, database));
			}
			
			for(Resource resource : resources){
				var tags = fetchTagsForResources(connection, TargetPath.normalizePath(resource.resourcePath().toString()));
				resource.setTags(tags.keySet());
			}
			return resources;
		} catch(SQLException e){
			throw new CoreSqlException("Failed to get all resources", e);
		}
	}
	
	private static Map<TagId, Tag> fetchTagsForResources(Connection connection, String path) throws SQLException {
		Map<TagId, Tag> tags = new HashMap<>();
		String query = """
				SELECT Tags.tag_id, tag_name
				FROM ResourceTags
				JOIN Tags ON ResourceTags.tag_id = Tags.tag_id
				WHERE resource_path = ?""";
		try(PreparedStatement statement = connection.prepareStatement(query)){
			statement.setString(1, TargetPath.normalizePath(path));
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				TagId tagId = new TagId(resultSet.getString(1));
				tags.put(tagId, new Tag(tagId, resultSet.getString(2)));
			}
		}
		return tags;
	}
	
	private List<Tag> getAllTags(Connection connection) throws CoreSqlException {
		List<Tag> tags = new ArrayList<>();
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM Tags")){
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				tags.add(new Tag(new TagId(resultSet.getString("tag_id")), resultSet.getString("tag_name")));
			}
			return tags;
		} catch(Exception e){
			throw new CoreSqlException("Failed to get all tags", e);
		}
	}
	
	private static Resource resourceFromResultSet(ResultSet resultSet, Set<TagId> tags, String data, RepositoryDatabase database)
			throws SQLException {
		return new Resource(Path.of(resultSet.getString("resource_path")),
				DateHelper.parseDateTime(resultSet.getString("created_at")),
				resultSet.getString("created_by"),
				DateHelper.parseDateTime(resultSet.getString("last_modified_at")),
				resultSet.getString("last_modified_by"),
				database.getRepoProperties().getId(),
				new HashSet<>(tags),
				resultSet.getString("category"),
				data);
	}
	
	/**
	 * Finds all resources with the matching search term in its data
	 *
	 * @param request the resource request
	 */
	public Map<Path, String> findByContent(ResourceRequest request) throws CoreException {
		String sqlScript;
		
		if(request.getSearchTerm() == null){
			sqlScript = """
					SELECT FileData.resource_path,
					       CASE
					           WHEN ? IS NOT NULL THEN data
					           WHEN ? IS NOT NULL THEN data --just temp to match the same parameters
					           END AS fileContent
					  FROM FileData
					 WHERE FileData.resource_path LIKE ?
					 LIMIT ?;
					""";
		} else {
			if(request.getSearchTerm().length() > 3){
				sqlScript = """
						SELECT FileData.resource_path,
						       CASE
						           WHEN ? IS NOT NULL THEN data
						           END AS fileContent
						  FROM FileData
						 WHERE data MATCH ?
						   AND FileData.resource_path LIKE ?
						 LIMIT ?;
						""";
			} else {
				sqlScript = """
						SELECT FileData.resource_path,
						       CASE
						           WHEN ? IS NOT NULL THEN data
						           END AS fileContent
						  FROM FileData
						 WHERE data LIKE '%' || ? || '%'
						   AND FileData.resource_path LIKE ?
						 LIMIT ?;
						""";
			}
		}
		
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement(sqlScript)){
			Map<Path, String> resources = new HashMap<>();
			statement.setString(1, request.isWithData() ? "anything" : null);
			statement.setString(2, request.getSearchTerm());
			statement.setString(3, DbHelper.convertAntPathToSQLLike(request.getPath()));
			statement.setInt(4, request.getReturnLimit());
			ResultSet resultSet = statement.executeQuery();
			
			while(resultSet.next()){
				resources.put(Path.of(resultSet.getString("resource_path")), resultSet.getString("fileContent"));
			}
			
			if(resources.isEmpty()){
				return new HashMap<>();
			}
			
			return resources;
			
		} catch(Exception e){
			log.error("Failed to find resource by content", e);
			throw new CoreSqlException("An unexpected error occured while searching resources!", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Fetches a resource from the database
	 *
	 * @param connection the connection to the database
	 * @param path the path to the resource
	 * @return the resource or null if it does not exist
	 */
	private Resource getResource(Connection connection, Path path) {
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM Resources WHERE resource_path = ?")){
			statement.setString(1, path.toString());
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next()){
				var tags = fetchTagsForResources(connection, path.toString());
				return resourceFromResultSet(resultSet, tags.keySet(), null, database);
			}
			return null;
		} catch(SQLException e){
			log.error("Failed to get resource", e);
			return null;
		}
	}
	
	/**
	 * Updates the data of a resource in the database
	 *
	 * @param connection the connection to the database
	 * @param resourcePath the path to the resource
	 * @param data the data to update the resource with
	 * @throws CoreSqlException
	 */
	private void updateResourceData(Connection connection, Path resourcePath, String data) throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("UPDATE FileData SET data = ? WHERE resource_path = ?")){
			statement.setString(1, data);
			statement.setString(2, resourcePath.toString());
			statement.executeUpdate();
		} catch(Exception e){
			String errorResponse = "Failed to update resource data at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Updates the tags of a resource (removes all existing tags and replaces them with the new tags)
	 *
	 * @param connection
	 * @param resourcePath
	 * @param tags
	 * @return
	 */
	private void updateResourceTagsSet(Connection connection, Path resourcePath, Set<TagId> tags) throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM ResourceTags WHERE resource_path = ?")){
			statement.setString(1, resourcePath.toString());
			statement.executeUpdate();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			throw new CoreSqlException(errorResponse, e);
		}
		
		try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ResourceTags(resource_path, tag_id) VALUES(?, ?)")){
			for(var tag : tags){
				statement.setString(1, resourcePath.toString());
				statement.setString(2, tag.id());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Adds missing tags to the database
	 *
	 * @param connection the connection to the database
	 * @param tags the tags to add
	 */
	private void addMissingTags(Connection connection, List<Tag> tags) throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO Tags(tag_id, tag_name) VALUES(?, ?)")){
			for(Tag tag : tags){
				statement.setString(1, tag.tagId().id());
				statement.setString(2, tag.tagName());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch(Exception e){
			String errorResponse = "Failed to add missing tags";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Updates the tags of a resource (removes the tags from the resource)
	 *
	 * @param connection
	 * @param resourcePath
	 * @param tags
	 * @return
	 */
	private void updateResourceTagsRemove(Connection connection, Path resourcePath, Set<TagId> tags) throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM ResourceTags WHERE resource_path = ? AND tag_id = ?")){
			for(var tag : tags){
				statement.setString(1, resourcePath.toString());
				statement.setString(2, tag.id());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Updates the tags of a resource (adds the tags to the resource)
	 *
	 * @param connection
	 * @param resourcePath
	 * @param tags
	 * @return
	 */
	private void updateResourceTagsAdd(Connection connection, Path resourcePath, Set<TagId> tags) throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ResourceTags(resource_path, tag_id) VALUES(?, ?)")){
			for(var tag : tags){
				statement.setString(1, resourcePath.toString());
				statement.setString(2, tag.id());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	
	/**
	 * Batch inserts a list of resources into the database
	 *
	 * @param resources the resources to insert
	 */
	public void batchInsertResources(List<Resource> resources) throws CoreSqlException {
		Connection connection = database.getConnection();
		try{
			int affectedRows = 0;
			try(var statement = connection.prepareStatement(
					"INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category)VALUES(?, ?, ?, ?, ?, ?)")){
				connection.setAutoCommit(false);
				for(var resource : resources){
					statement.setString(1, resource.resourcePath().toString());
					statement.setString(2, DateHelper.fromDateTime(resource.createdAt()));
					statement.setString(3, resource.createdBy());
					statement.setString(4, DateHelper.fromDateTime(resource.modifiedAt()));
					statement.setString(5, resource.modifiedBy());
					statement.setString(6, resource.category());
					statement.addBatch();
				}
				affectedRows += Arrays.stream(statement.executeBatch()).sum();
			}
			
			try(var statement = connection.prepareStatement("INSERT INTO FileData(resource_path, data )VALUES(?, ?)")){
				for(var resource : resources){
					if(resource.data() == null){
						continue;
					}
					statement.setString(1, resource.resourcePath().toString());
					statement.setString(2, resource.data());
					statement.addBatch();
				}
				affectedRows += Arrays.stream(statement.executeBatch()).sum();
				connection.commit();
			}
		} catch(Exception e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("This should not happen", ex);
			}
			log.error("Failed to batch insert resources", e);
			throw new CoreSqlException("Failed to batch insert resources", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Batch updates a list of resources in the database.
	 *
	 * @param resources the resources to update
	 */
	public void batchUpdateResources(List<Resource> resources) throws CoreSqlException {
		Connection connection = database.getConnection();
		try{
			int affectedRows = 0;
			try(var statement = connection.prepareStatement("UPDATE Resources " +
															"SET last_modified_at = ?, last_modified_by = ?, category = ?" +
															"WHERE resource_path = ?")){
				
				connection.setAutoCommit(false);
				for(var resource : resources){
					statement.setString(1, DateHelper.fromDateTime(resource.modifiedAt()));
					statement.setString(2, resource.modifiedBy());
					statement.setString(3, resource.category());
					statement.setString(4, resource.resourcePath().toString());
					statement.addBatch();
				}
				affectedRows += Arrays.stream(statement.executeBatch()).sum();
			}
			
			//deleting and reinserting the filedata
			try(var deleteStatement = connection.prepareStatement("DELETE FROM FileData WHERE resource_path = ?");
				var insertStatement = connection.prepareStatement("INSERT INTO FileData(resource_path, data) VALUES(?, ?)")){
				
				for(var resource : resources){
					if(resource.data() == null){
						continue;
					}
					
					// Delete existing entry
					deleteStatement.setString(1, resource.resourcePath().toString());
					deleteStatement.addBatch();
					
					// Reinsert new data
					insertStatement.setString(1, resource.resourcePath().toString());
					insertStatement.setString(2, resource.data());
					insertStatement.addBatch();
				}
				
				deleteStatement.executeBatch(); // Ensure deletions are executed first
				affectedRows += Arrays.stream(insertStatement.executeBatch()).sum();
			}
			
			connection.commit();
		} catch(Exception e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("Rollback failed", ex);
			}
			throw new CoreSqlException("Failed to batch update resources", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Batch deletes a list of resources from the database.
	 *
	 * @param resourcePaths the list of resource paths to delete
	 */
	private void batchDeleteResources(List<Path> resourcePaths) throws CoreSqlException {
		Connection connection = database.getConnection();
		try{
			int affectedRows = 0;
			connection.setAutoCommit(false);
			
			// Delete from the FTS table (FileData) first
			try(var deleteFileDataStmt = connection.prepareStatement("DELETE FROM FileData WHERE resource_path = ?")){
				for(var resourcePath : resourcePaths){
					deleteFileDataStmt.setString(1, resourcePath.toString());
					deleteFileDataStmt.addBatch();
				}
				affectedRows += Arrays.stream(deleteFileDataStmt.executeBatch()).sum();
			}
			
			// Delete from the Resources table
			try(var deleteResourcesStmt = connection.prepareStatement("DELETE FROM Resources WHERE resource_path = ?")){
				for(var resourcePath : resourcePaths){
					deleteResourcesStmt.setString(1, resourcePath.toString());
					deleteResourcesStmt.addBatch();
				}
				affectedRows += Arrays.stream(deleteResourcesStmt.executeBatch()).sum();
			}
			
			connection.commit();
		} catch(Exception e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("Rollback failed", ex);
			}
			throw new CoreSqlException("Failed to batch delete resources", e);
		} finally{
			closeConnection(connection);
		}
	}
	

	
	@Override
	public List<Resource> getResources(ResourceRequest request) throws CoreException {
		Map<Path, Resource> resources = new HashMap<>(resourceCache);
		
		if(request.getSearchTerm() != null || request.isWithData()){
			Map<Path, String> content = findByContent(request);
			
			resources = new HashMap<>();
			for(var entry : resourceCache.entrySet()){
				if(content.containsKey(entry.getKey())){
					resources.put(entry.getKey(), entry.getValue().copy().setData(content.get(entry.getKey())));
				}
			}
		}
		
		// Apply path filtering only if necessary
		if(request.targetPath().isPresent()){
			resources = resources.entrySet().stream().filter(entry -> pathMatcher.match(request.getPath(), entry.getKey().toString())).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		
		resources = resources.entrySet()
							 .stream()
							 .filter(entry -> request.whiteListTags() == null ||
											  request.whiteListTags().isEmpty() ||
											  entry.getValue().hasAnyTagId(request.whiteListTags()))
							 .filter(entry -> request.blacklistTags() == null ||
											  request.blacklistTags().isEmpty() ||
											  !entry.getValue().hasAnyTagId(request.blacklistTags()))
							 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new ArrayList<>(resources.values());
	}
	
	@Override
	public void insertResource(Resource resource) throws ClientException, CoreException {
		Connection connection = database.getConnection();
		try{
			//todo:jmd make it 1 big transaction in case someting goes wrong.
			
			int affectedRows = 0;
			boolean resourceExists = false;
			
			try(var statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM Resources WHERE resource_path = ?)")){
				statement.setString(1, resource.resourcePath().toString());
				ResultSet resultSet = statement.executeQuery();
				resourceExists = resultSet.next() && resultSet.getBoolean(1);
			} catch(Exception e){
				throw new CoreSqlException("Failed to check if resource exists", e);
			}
			
			if(resourceExists){
				throw new ClientException("Resource already exists");
			}
			
			String sqlResourceInsert = """
					
					    INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category)
					VALUES(?, ?, ?, ?, ?, ?)
					
					""";
			try(PreparedStatement statement = connection.prepareStatement(sqlResourceInsert)){
				statement.setString(1, resource.resourcePath().toString());
				statement.setString(2, DateHelper.fromDateTime(resource.createdAt()));
				statement.setString(3, resource.createdBy());
				statement.setString(4, DateHelper.fromDateTime(resource.modifiedAt()));
				statement.setString(5, resource.modifiedBy());
				statement.setString(6, resource.category());
				affectedRows = statement.executeUpdate();
			} catch(Exception e){
				log.error("Failed to insert resource", e);
				throw new CoreSqlException("An unexpected error occured while inserting resource!", e);
			}
			
			if(resource.data() == null){ //no data to insert so we skip the next part
				resourceCache.put(resource.resourcePath(), resource);
				return;
			}
			
			String sqlDataInsert = """
					INSERT INTO
					FileData(resource_path, data)
					VALUES(?,?)
					""";
			try(PreparedStatement statement = connection.prepareStatement(sqlDataInsert)){
				statement.setString(1, resource.resourcePath().toString());
				statement.setString(2, resource.data());
				affectedRows += statement.executeUpdate();
			} catch(Exception e){
				log.error("Failed to insert resource data", e);
				throw new CoreSqlException("An unexpected error occured while inserting resource data!", e);
			}
			
			Set<TagId> resourceTags = resource.getResourceTags();
			if(resourceTags != null && resourceTags.isEmpty()){
				
				String sqlTagInsert = """
						INSERT INTO ResourceTags(resource_path, tag_id)
						VALUES(?,?)
						""";
				try(PreparedStatement statement = connection.prepareStatement(sqlTagInsert)){
					for(TagId tagId : resourceTags){
						statement.setString(1, resource.resourcePath().toString());
						statement.setString(2, tagId.id());
						statement.addBatch();
					}
					affectedRows += Arrays.stream(statement.executeBatch()).sum();
				} catch(Exception e){
					log.error("Failed to insert resource tags", e);
					throw new CoreSqlException("An unexpected error occured while inserting resource tags!", e);
				}
				
			}
			resourceCache.put(resource.resourcePath(), resource);
			
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean removeResource(RepoId repoId, Path path) throws CoreSqlException {
		log.info("Removing resource at path '{}' for '{}'", path, repoId);
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM FileData WHERE resource_path = ?")){
			statement.setString(1, TargetPath.normalizePath(path.toString()));
			statement.executeUpdate();
			resourceCache.remove(path);
			log.info("Resource at path '{}' for '{}' removed", path, repoId);
			return true;
		} catch(Exception e){
			throw new CoreSqlException("Failed to delete resource", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public Resource updateResource(ResourceUpdateRequest request) throws CoreSqlException {
		log.info("Updating resource '{}' for '{}'", request.path(), database.getRepoId());
		Connection connection = database.getConnection();
		try{
			connection.setAutoCommit(false);
			if(request.getData() != null){
				updateResourceData(connection, request.path(), request.getData());
			}
			
			if(request.tagsToSet() != null){
				updateResourceTagsSet(connection, request.path(), request.tagsToSet());
			}
			
			if(request.tagsToRemove() != null && !request.tagsToRemove().isEmpty()){
				updateResourceTagsRemove(connection, request.path(), request.tagsToRemove());
			}
			
			if(request.tagsToAdd() != null && !request.tagsToAdd().isEmpty()){
				updateResourceTagsAdd(connection, request.path(), request.tagsToAdd());
			}
			
			if(request.getCategory() == null && request.isTreatNullsAsValues()){
				try(PreparedStatement statement = connection.prepareStatement("UPDATE Resources SET category = ? WHERE resource_path = ?")){
					statement.setString(1, request.getCategory());
					statement.setString(2, request.path().toString());
				} catch(Exception e){
					throw new CoreSqlException("Failed to update resource '%s'".formatted(request.path()), e);
				}
			}
			
			try(var statement = connection.prepareStatement("UPDATE Resources " +
															"SET last_modified_at = ?, last_modified_by = ?" +
															"WHERE resource_path = ?")){
				statement.setString(1, DateHelper.fromDateTime(LocalDateTime.now()));
				statement.setString(2, request.userId().id());
				statement.setString(3, request.path().toString());
				
				statement.executeUpdate();
				
				connection.commit();
				
				//gets the updated resource
				Resource resource = getResource(connection, request.path());
				if(resource == null){
					throw new CoreSqlException("Failed to update resource '%s'".formatted(request.path()));
				}
				
				resourceCache.put(request.path(), resource);
				log.info("Resource '{}' updated for '{}'", request.path(), database.getRepoId());
				return resource;
			}
		} catch(SQLException e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("Failed to rollback transaction", ex);
			}
			log.error("Failed to update resource '{}'", request.path(), e);
			throw new CoreSqlException("Failed to update resource '%s'".formatted(request.path()), e);
		} finally{
			try{
				connection.setAutoCommit(true);
				connection.close();
			} catch(SQLException e){
				log.error("Failed to close connection", e);
			}
		}
	}
	
	@Override
	public boolean resourceExists(RepoId repoId, Path path) {
		return resourceCache.containsKey(path);
	}
	
	@Override
	public boolean moveResource(RepoId repoId, Path oldPath, Path newPath) throws CoreSqlException {
		log.info("Updating resource path '{}' to '{}'", oldPath, newPath);
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("UPDATE Resources SET resource_path = ? WHERE resource_path = ?")){
			statement.setString(1, newPath.toString());
			statement.setString(2, oldPath.toString());
			statement.executeUpdate();
			Resource resource = resourceCache.remove(oldPath);
			resource.setResourcePath(newPath);
			resourceCache.put(newPath, resource);
			log.info("Resource path updated from '{}' to '{}'", oldPath, newPath);
			return true;
		} catch(Exception e){
			String errorResponse = "Failed to update resource path from '%s' to '%s'".formatted(oldPath, newPath);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Batch deletes resources from the database
	 *
	 * @param resources the resources to delete
	 * @return the row count affected, -1 if an error occurred
	 */
	public void batchDelete(List<Path> resources) throws CoreSqlException {
		log.info("Batch deleting resources for repo '{}'", database.getRepoId());
		batchDeleteResources(resources);
		resources.forEach(resourceCache::remove);
		log.info("Batch deleted resources for repo '{}'", database.getRepoId());
	}
	
	/**
	 * Batch updates resources in the database
	 *
	 * @param resources the resources to update
	 * @return the row count affected, -1 if an error occurred
	 */
	public void batchUpdate(List<Resource> resources) throws CoreSqlException {
		log.info("Batch updating resources for repo '{}'", database.getRepoId());
		batchUpdateResources(resources);
		resources.forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
	}
	
	/**
	 * Batch inserts resources into the database
	 *
	 * @param resources the resources to insert
	 * @return the row count affected, -1 if an error occurred
	 */
	public void batchInsert(List<Resource> resources) throws CoreSqlException {
		log.info("Batch inserting resources for repo '{}'", database.getRepoId());
		batchInsertResources(resources);
		resources.forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
	}
	
	@Override
	public List<Tag> getTags(RepoId repoId) throws InvalidRepoException {
		return new ArrayList<>(tagCache.values());
	}
	
	@Override
	public void createTag(RepoId repoId, Tag tag) throws CoreSqlException {
		log.info("Adding tag '{}' to '{}'", tag.tagId(), repoId);
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("INSERT INTO Tags(tag_id, tag_name) VALUES(?, ?)")){
			statement.setString(1, tag.tagId().id());
			statement.setString(2, tag.tagName());
			statement.executeUpdate();
			tagCache.put(tag.tagId(), tag);
			log.info("Tag '{}' added to '{}'", tag.tagId(), repoId);
		} catch(Exception e){
			throw new CoreSqlException("Failed to add tag '%s' to '%s'".formatted(tag.tagId(), database.getRepoId()), e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public void removeTag(RepoId repoId, TagId tagId) throws CoreSqlException {
		log.info("Removing tag {} for repo {}", tagId, repoId);
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM Tags WHERE tag_id = ?")){
			statement.setString(1, tagId.id());
			statement.executeUpdate();
			tagCache.remove(tagId);
			//remove tags from resource cache
			resourceCache.values().forEach(r -> r.getResourceTags().remove(tagId));
			log.info("Tag {} removed for repo {}", tagId, repoId);
		} catch(Exception e){
			throw new CoreSqlException("Failed to remove tag '%s'".formatted(tagId.id()), e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean tagExists(RepoId repoId, TagId tagId) {
		if(tagId == null){
			return false;
		}
		return tagCache.containsKey(tagId);
	}
	
	@Override
	public UserId getEditingUser(RepoId repoId, Path path) {
		return currentlyEdited.entrySet().stream().filter(p -> p.getValue().equals(path)).map(Map.Entry::getKey).findFirst().orElse(null);
	}
	
	@Override
	public void removeCurrentlyEdited(RepoId repoId, UserId userId) {
		currentlyEdited.remove(userId);
	}
	
	@Override
	public void removeCurrentlyEdited(RepoId id, Path path) {
		currentlyEdited.values().removeIf(p -> p.equals(path));
	}
	
	@Override
	public boolean isUserEditing(RepoId id, UserId userId) {
		return currentlyEdited.containsKey(userId);
	}
	
	@Override
	public void setCurrentlyEdited(RepoId repoId, UserId userId, Path path) {
		currentlyEdited.put(userId, path);
	}
	
	private void closeConnection(Connection connection) {
		try{
			connection.close();
		} catch(SQLException e){
			log.error("Error while closing connection", e);
		}
	}
}

