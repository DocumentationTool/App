package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.DbHelper;
import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.objects.DateHelper;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * All resource related database functions
 */
public class ResourceFunctions{
	private static final Logger log = LoggerFactory.getLogger(ResourceFunctions.class);
	
	private ResourceFunctions() {
		//Utility Class
	}
	
	/**
	 * Deletes a specific resource and all its related data in ResourceData, Tags and Permissions
	 *
	 * @param resourcePath the path to the resource
	 */
	public static void deleteResource(RepositoryDatabase database, Path resourcePath) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM FileData WHERE resource_path = ?")){
			statement.setString(1, resourcePath.toString());
			statement.executeUpdate();
		} catch(Exception e){
			log.error("Failed to delete resource", e);
			throw new CoreSqlException("Failed to delete resource", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Retrieves a list of all resources contained in the given repository databases table(without its content attached)
	 */
	public static List<Resource> getAllResources(RepositoryDatabase database) throws CoreSqlException {
		Connection connection = database.getConnection();
		
		List<Resource> resources = new ArrayList<>();
		String query = "SELECT resource_path, created_at, created_by, last_modified_at, last_modified_by, category " + "FROM Resources";
		try(PreparedStatement statement = connection.prepareStatement(query)){
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				resources.add(resourceFromResultSet(resultSet, new HashSet<>(), null, database));
			}
			
			for(Resource resource : resources){
				var tags = fetchTagsForResources(connection, resource.resourcePath().toString());
				resource.setTags(tags.keySet());
			}
			return resources;
		} catch(SQLException e){
			log.error("Failed to get all resources", e);
			throw new CoreSqlException("Failed to get all resources", e);
		} finally{
			closeConnection(connection);
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
			statement.setString(1, path);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				TagId tagId = new TagId(resultSet.getString(1));
				tags.put(tagId, new Tag(tagId, resultSet.getString(2)));
			}
		}
		return tags;
	}
	
	public static List<Tag> getAllTags(RepositoryDatabase database) throws CoreSqlException {
		List<Tag> tags = new ArrayList<>();
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM Tags")){
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				tags.add(new Tag(new TagId(resultSet.getString("tag_id")), resultSet.getString("tag_name")));
			}
			return tags;
		} catch(Exception e){
			throw new CoreSqlException("Failed to get all tags", e);
		} finally{
			closeConnection(connection);
		}
		
	}
	
	/**
	 * Adds a tag to the database
	 *
	 * @param database the database to add the tag to
	 * @param tag the tag to add
	 */
	public static void addTag(RepositoryDatabase database, Tag tag) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("INSERT INTO Tags(tag_id, tag_name) VALUES(?, ?)")){
			statement.setString(1, tag.tagId().id());
			statement.setString(2, tag.tagName());
			statement.executeUpdate();
		} catch(Exception e){
			log.error("Failed to add tag", e);
			throw new CoreSqlException("Failed to add tag '%s' to '%s'".formatted(tag.tagId(), database.getRepoId()), e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Removes a tag from the database
	 *
	 * @param database the database to remove the tag from
	 * @param tagId the tag to remove
	 */
	public static void removeTag(RepositoryDatabase database, TagId tagId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM Tags WHERE tag_id = ?")){
			statement.setString(1, tagId.id());
			statement.executeUpdate();
		} catch(Exception e){
			throw new CoreSqlException("Failed to remove tag '%s'".formatted(tagId.id()), e);
		} finally{
			closeConnection(connection);
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
	public static Map<Path, String> findByContent(RepositoryDatabase database, ResourceRequest request) throws CoreException {
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
	 * Inserts a new resource into the database also inserts the data into the FileData table if it was set
	 *
	 * @param resource the resource to add
	 */
	public static void insertResource(RepositoryDatabase database, Resource resource) throws CoreException {
		Connection connection = database.getConnection();
		try{
			
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
				throw new CoreException("Resource already exists");
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
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Updates the path of a resource to the new path (automatically updates all relevant paths duo to triggers generated by {@link DatabaseFunctions}
	 *
	 * @param oldPath the path to change
	 * @param newPath the path to change it to
	 */
	public static void updatePath(RepositoryDatabase database, Path oldPath, Path newPath) throws CoreSqlException {
		log.info("Updating resource path '{}' to '{}'", oldPath, newPath);
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("UPDATE Resources SET resource_path = ? WHERE resource_path = ?")){
			statement.setString(1, newPath.toString());
			statement.setString(2, oldPath.toString());
		} catch(Exception e){
			String errorResponse = "Failed to update resource path from '%s' to '%s'".formatted(oldPath, newPath);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Updates a resources data
	 *
	 * @param request the resource request
	 */
	public static Resource updateResource(RepositoryDatabase database, ResourceUpdateRequest request) throws CoreSqlException {
		Connection connection = database.getConnection();
		try{
			connection.setAutoCommit(false);
			if(request.getData() != null){
				updateResourceData(connection, database, request.path(), request.getData());
			}
			
			if(request.tagsToSet() != null){
				updateResourceTagsSet(connection, database, request.path(), request.tagsToSet());
			}
			
			if(request.tagsToRemove() != null && !request.tagsToRemove().isEmpty()){
				updateResourceTagsRemove(connection, database,request.path(), request.tagsToRemove());
			}
			
			if(request.tagsToAdd() != null && !request.tagsToAdd().isEmpty()){
				updateResourceTagsAdd(connection, database, request.path(), request.tagsToAdd());
			}
			
			if(request.getCategory() == null && request.isTreatNullsAsValues()){
				try(PreparedStatement statement = connection.prepareStatement("UPDATE Resources SET category = ? WHERE resource_path = ?")){
					statement.setString(1, request.getCategory());
					statement.setString(2, request.path().toString());
				} catch(Exception e){
					String errorResponse = "Failed to update resource '%s'".formatted(request.path());
					log.error(errorResponse, e);
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
				Resource resource = getResource(connection, database,request.path());
				if(resource == null){
					throw new CoreSqlException("Failed to update resource '%s'".formatted(request.path()));
				}
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
	
	/**
	 * Fetches a resource from the database
	 *
	 * @param connection the connection to the database
	 * @param database the database to fetch the resource from
	 * @param path the path to the resource
	 * @return the resource or null if it does not exist
	 */
	private static Resource getResource(Connection connection, RepositoryDatabase database, Path path) {
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
	
	private static void updateResourceData(Connection connection, RepositoryDatabase database, Path resourcePath, String data)
			throws CoreSqlException {
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
	 * @param database
	 * @param resourcePath
	 * @param tags
	 * @return
	 */
	private static void updateResourceTagsSet(Connection connection, RepositoryDatabase database, Path resourcePath, Set<TagId> tags)
			throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM ResourceTags WHERE resource_path = ?")){
			statement.setString(1, resourcePath.toString());
			statement.executeUpdate();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
		
		try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ResourceTags(resource_path, tag_id) VALUES(?, ?)")){
			for(var tag : tags){
				statement.setString(1, resourcePath.toString());
				statement.setString(2, tag.id());
				statement.addBatch();
			}
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Adds missing tags to the database
	 *
	 * @param connection the connection to the database
	 * @param tags the tags to add
	 */
	private static void addMissingTags(Connection connection, List<Tag> tags) throws CoreSqlException {
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
	 * @param database
	 * @param resourcePath
	 * @param tags
	 * @return
	 */
	private static void updateResourceTagsRemove(Connection connection, RepositoryDatabase database, Path resourcePath, Set<TagId> tags)
			throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("DELETE FROM ResourceTags WHERE resource_path = ? AND tag_id = ?")){
			for(var tag : tags){
				statement.setString(1, resourcePath.toString());
				statement.setString(2, tag.id());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Updates the tags of a resource (adds the tags to the resource)
	 *
	 * @param connection
	 * @param database
	 * @param resourcePath
	 * @param tags
	 * @return
	 */
	private static void updateResourceTagsAdd(Connection connection, RepositoryDatabase database, Path resourcePath, Set<TagId> tags)
			throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ResourceTags(resource_path, tag_id) VALUES(?, ?)")){
			for(var tag : tags){
				statement.setString(1, resourcePath.toString());
				statement.setString(2, tag.id());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch(Exception e){
			String errorResponse = "Failed to update resource tags at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	//todo:jmd check commits, if the commit matches the current commit then ignore as its already the same
	
	/**
	 * Batch inserts a list of resources into the database
	 *
	 * @param database the database to insert the resources into
	 * @param resources the resources to insert
	 */
	public static void batchInsertResources(RepositoryDatabase database, List<Resource> resources) throws CoreSqlException {
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
	 * @param database the database to update the resources in
	 * @param resources the resources to update
	 */
	public static void batchUpdateResources(RepositoryDatabase database, List<Resource> resources) throws CoreSqlException {
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
			log.error("Failed to batch update resources", e);
			throw new CoreSqlException("Failed to batch update resources", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Batch deletes a list of resources from the database.
	 *
	 * @param database the database to delete the resources from
	 * @param resourcePaths the list of resource paths to delete
	 */
	public static void batchDeleteResources(RepositoryDatabase database, List<Path> resourcePaths) throws CoreSqlException {
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
			log.error("Failed to batch delete resources", e);
			throw new CoreSqlException("Failed to batch delete resources", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	private static void closeConnection(Connection connection) {
		try{
			connection.close();
		} catch(SQLException e){
			log.error("Error while closing connection", e);
		}
	}
}

