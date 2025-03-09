package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.Resource;
import static com.wonkglorg.doc.core.objects.Resource.fromDateTime;
import static com.wonkglorg.doc.core.objects.Resource.parseDateTime;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse deleteResource(RepositoryDatabase database, Path resourcePath) {
		try(PreparedStatement statement = database.getConnection().prepareStatement("DELETE FROM FileData WHERE resource_path = ?")){
			statement.setString(1, resourcePath.toString());
			int i = statement.executeUpdate();
			if(i == 0){
				return UpdateDatabaseResponse.fail(database.getRepoId(), new IllegalArgumentException("Resource does not exist"));
			}
			return UpdateDatabaseResponse.success(database.getRepoId(), "Successfully deleted resource '%s'".formatted(resourcePath.toString()), i);
		} catch(Exception e){
			log.error("Failed to delete resource", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to delete resource", e));
		}
	}
	
	/**
	 * Retrieves a list of all resources contained in the given repository databases table(without its content attached)
	 *
	 * @return {@link QueryDatabaseResponse}
	 */
	public static QueryDatabaseResponse<List<Resource>> getResources(RepositoryDatabase database, ResourceRequest request) {
		List<Resource> resources;
		Connection connection = database.getConnection();
		
		try{
			resources = fetchResources(connection, database, request.path, request.returnLimit);
			
			for(Resource resource : resources){
				String resourceData = request.withData ? fetchResourceData(connection, resource.resourcePath().toString()) : null;
				
				List<Tag> tags = fetchTagsForResources(connection, resource.resourcePath().toString());
				
				populateResourceInfo(resource, tags, resourceData);
			}
			
			return QueryDatabaseResponse.success(database.getRepoId(), resources);
		} catch(SQLException e){
			log.error("Failed to get resources", e);
			return QueryDatabaseResponse.error(database.getRepoId(), new RuntimeSQLException("Failed to get resources", e));
		} finally{
			try{
				connection.close();
			} catch(SQLException e){
				log.error("Failed to Close connection", e);
			}
		}
	}
	
	private static List<Resource> fetchResources(Connection connection, RepositoryDatabase database, String path, int limit) throws SQLException {
		int currentFetchIndex = 0;
		List<Resource> resources = new ArrayList<>();
		String query = "SELECT resource_path, created_at, created_by, last_modified_at, last_modified_by, category, commit_id " +
					   "FROM Resources WHERE resource_path LIKE ?";
		try(PreparedStatement statement = connection.prepareStatement(query)){
			statement.setString(1, path == null ? "%" : path);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				if(currentFetchIndex++ > limit){
					return resources;
				}
				resources.add(resourceFromResultSet(resultSet, null, null, database));
			}
		}
		return resources;
	}
	
	private static String fetchResourceData(Connection connection, String path) throws SQLException {
		String resourceData = null;
		String query = "SELECT data FROM FileData WHERE resource_path = ?";
		try(PreparedStatement statement = connection.prepareStatement(query)){
			statement.setString(1, path);
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next()){
				resourceData = resultSet.getString(1);
			}
		}
		return resourceData;
	}
	
	private static List<Tag> fetchTagsForResources(Connection connection, String path) throws SQLException {
		List<Tag> tags = new ArrayList<>();
		String query = """
				SELECT Tags.tag_id, tag_name
				FROM ResourceTags
				JOIN Tags ON ResourceTags.tag_id = Tags.tag_id
				WHERE resource_path = ?""";
		try(PreparedStatement statement = connection.prepareStatement(query)){
			statement.setString(1, path);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				tags.add(new Tag(resultSet.getString(1), resultSet.getString(2)));
			}
		}
		return tags;
	}
	
	private static void populateResourceInfo(Resource resource, List<Tag> tags, String resourceData) {
		// Assuming resource has methods to add tags and data, adjust as necessary
		resource.setTags(tags);
		resource.setData(resourceData);
	}
	
	private static Resource resourceFromResultSet(ResultSet resultSet, List<Tag> tags, String data, RepositoryDatabase database) throws SQLException {
		return new Resource(Path.of(resultSet.getString("resource_path")),
				parseDateTime(resultSet.getString("created_at")),
				resultSet.getString("created_by"),
				parseDateTime(resultSet.getString("last_modified_at")),
				resultSet.getString("last_modified_by"),
				database.getRepoProperties().getId(),
				tags,
				database.getRepoProperties().isReadOnly(),
				resultSet.getString("category"),
				data);
	}
	
	/**
	 * Finds a resource by its fully Qualified path
	 *
	 * @param resourcePath the path to search for
	 * @return {@link QueryDatabaseResponse}
	 */
	public static QueryDatabaseResponse<Resource> findByPath(RepositoryDatabase database, Path resourcePath) {
		try(PreparedStatement statement = database.getConnection().prepareStatement(
				"SELECT resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id FROM Resources WHERE resource_path = ?")){
			statement.setString(1, resourcePath.toString());
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next()){
				return QueryDatabaseResponse.success(database.getRepoId(), resourceFromResultSet(resultSet, null, null, database));
			}
		} catch(Exception e){
			log.error("Failed to find resource by path", e);
			return QueryDatabaseResponse.error(database.getRepoId(),
					new RuntimeSQLException("Failed to find resource by path '%s'".formatted(resourcePath), e));
		}
		return QueryDatabaseResponse.success(database.getRepoId(), "No resource matching path %s".formatted(resourcePath), null);
	}
	
	public static QueryDatabaseResponse<Boolean> resourceExists(RepositoryDatabase database, Path path) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM Resources WHERE resource_path = ?)")){
			statement.setString(1, path.toString());
			ResultSet resultSet = statement.executeQuery();
			if(resultSet.next()){
				return QueryDatabaseResponse.success(database.getRepoId(), resultSet.getBoolean(1));
			}
		} catch(Exception e){
			log.error("Failed to check if resource exists", e);
			return QueryDatabaseResponse.error(database.getRepoId(),
					new RuntimeSQLException("Failed to check if resource exists at path '%s'".formatted(path), e));
		} finally{
			try{
				connection.close();
			} catch(SQLException e){
				log.error("Failed to close connection", e);
			}
		}
		return QueryDatabaseResponse.success(database.getRepoId(), "No resource matching path %s".formatted(path), false);
	}
	
	public static QueryDatabaseResponse<List<Resource>> findByCategory(RepositoryDatabase database, String category) {
		return QueryDatabaseResponse.error(database.getRepoId(), new UnsupportedOperationException("Not implemented yet"));
	}
	
	public static QueryDatabaseResponse<List<Resource>> findByAntPath(RepositoryDatabase database, String antPath) {
		return QueryDatabaseResponse.error(database.getRepoId(), new UnsupportedOperationException("Not implemented yet"));
	}
	
	/**
	 * Finds all resources with the matching search term in its data
	 *
	 * @param searchTerm the term to search for
	 * @return {@link QueryDatabaseResponse}
	 */
	public static QueryDatabaseResponse<List<Resource>> findByContent(RepositoryDatabase database, String searchTerm) {
		String sqlScript = """
				SELECT Resources.resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id, data
				  FROM FileData
				  JOIN Resources
				    ON FileData.resource_path = Resources.resource_path
				 WHERE
				  CASE
				    WHEN length(?) >= 3
				      THEN data Match ?
				    ELSE data LIKE '%' || ? || '%'
				 END
				""";
		
		try(PreparedStatement statement = database.getConnection().prepareStatement(sqlScript)){
			List<Resource> resources = new ArrayList<>();
			statement.setString(1, searchTerm);
			statement.setString(2, searchTerm);
			statement.setString(3, searchTerm);
			ResultSet resultSet = statement.executeQuery();
			
			while(resultSet.next()){
				resources.add(resourceFromResultSet(resultSet, null, null, database));
			}
			
			if(resources.isEmpty()){
				return QueryDatabaseResponse.success(database.getRepoId(),
						"No files found matching search term: '%s'".formatted(searchTerm),
						resources);
			}
			
			return QueryDatabaseResponse.success(database.getRepoId(), resources);
			
		} catch(Exception e){
			log.error("Failed to find resource by content", e);
			return QueryDatabaseResponse.error(database.getRepoId(),
					new RuntimeSQLException("Failed to find resource by text '%s'".formatted(searchTerm), e));
		}
		
	}
	
	/**
	 * Inserts a new resource into the database also inserts the data into the FileData table if it was set
	 *
	 * @param resource the resource to add
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse insertResource(RepositoryDatabase database, Resource resource) {
		Connection connection = database.getConnection();
		int affectedRows = 0;
		boolean resourceExists = false;
		
		try(var statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM Resources WHERE resource_path = ?)")){
			statement.setString(1, resource.resourcePath().toString());
			ResultSet resultSet = statement.executeQuery();
			resourceExists = resultSet.next() && resultSet.getBoolean(1);
		} catch(Exception e){
			log.error("Failed to check if resource exists", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to check if resource exists", e));
		}
		
		if(resourceExists){
			return UpdateDatabaseResponse.fail(database.getRepoId(), new IllegalArgumentException("Resource already exists"));
		}
		
		String sqlResourceInsert = """
				INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category)
				VALUES(?, ?, ?, ?, ?, ?)
				""";
		try(PreparedStatement statement = connection.prepareStatement(sqlResourceInsert)){
			statement.setString(1, resource.resourcePath().toString());
			statement.setString(2, fromDateTime(resource.createdAt()));
			statement.setString(3, resource.createdBy());
			statement.setString(4, fromDateTime(resource.modifiedAt()));
			statement.setString(5, resource.modifiedBy());
			statement.setString(6, resource.category());
			affectedRows = statement.executeUpdate();
		} catch(Exception e){
			log.error("Failed to insert resource", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to insert resource", e));
		}
		
		if(resource.data() == null){ //no data to insert so we skip the next part
			return UpdateDatabaseResponse.success(database.getRepoId(), affectedRows);
		}
		
		String sqlDataInsert = """
				INSERT INTO FileData(resource_path, data)
				VALUES(?, ?)
				""";
		try(PreparedStatement statement = connection.prepareStatement(sqlDataInsert)){
			statement.setString(1, resource.resourcePath().toString());
			statement.setString(2, resource.data());
			affectedRows += statement.executeUpdate();
		} catch(Exception e){
			log.error("Failed to insert resource data", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to insert resource data", e));
		}
		return UpdateDatabaseResponse.success(database.getRepoId(), affectedRows);
	}
	
	/**
	 * Updates the path of a resource to the new path (automatically updates all relevant paths duo to triggers generated by {@link DatabaseFunctions}
	 *
	 * @param oldPath the path to change
	 * @param newPath the path to change it to
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse updatePath(RepositoryDatabase database, Path oldPath, Path newPath) {
		log.info("Updating resource path '{}' to '{}'", oldPath, newPath);
		try(PreparedStatement statement = database.getConnection()
												  .prepareStatement("UPDATE Resources SET resource_path = ? WHERE resource_path = ?")){
			statement.setString(1, newPath.toString());
			statement.setString(2, oldPath.toString());
			return UpdateDatabaseResponse.success(database.getRepoId(), statement.executeUpdate());
		} catch(Exception e){
			String errorResponse = "Failed to update resource path from '%s' to '%s'".formatted(oldPath, newPath);
			log.error(errorResponse, e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
	
	/**
	 * Updates a resources data
	 *
	 * @param resource the resource to update
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse updateResource(RepositoryDatabase database, Resource resource) {
		
		Connection connection = database.getConnection();
		
		//get current resource and compare it to the new resource
		
		/*
		try(var statement = connection.prepareStatement("UPDATE Resources " +
														"SET last_modified_at = ?, last_modified_by = ?, category = ?" +
														"WHERE resource_path = ?")){
			statement.setString(1, fromDateTime(resource.modifiedAt()));
			statement.setString(2, resource.modifiedBy());
			statement.setString(3, resource.category());
			statement.setString(4, resource.resourcePath().toString());
			int affectedRows = statement.executeUpdate();
			
			
			if(resource.data() == null){
				return UpdateDatabaseResponse.success(database.getRepoId(), affectedRows);
			}
			
			return updateResourceData(database, resource.resourcePath(), resource.data());
		} catch(Exception e){
			String errorResponse = "Failed to update resource '%s'".formatted(resource.resourcePath());
			log.error(errorResponse, e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
		try(var statement = connection.prepareStatement("UPDATE FileData SET data = ? WHERE resource_path = ?")){
			statement.setString(1, data);
			statement.setString(2, resourcePath.toString());
			return UpdateDatabaseResponse.success(database.getRepoId(), statement.executeUpdate());
		} catch(Exception e){
			String errorResponse = "Failed to update resource data at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
		
		 */
		return null;
	}
	
	//todo:jmd check commits, if the commit matches the current commit then ignore as its already the same
	
	/**
	 * Batch inserts a list of resources into the database
	 *
	 * @param database the database to insert the resources into
	 * @param resources the resources to insert
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse batchInsertResources(RepositoryDatabase database, List<Resource> resources) {
		Connection connection = database.getConnection();
		try{
			int affectedRows = 0;
			try(var statement = connection.prepareStatement(
					"INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category)VALUES(?, ?, ?, ?, ?, ?)")){
				connection.setAutoCommit(false);
				for(var resource : resources){
					statement.setString(1, resource.resourcePath().toString());
					statement.setString(2, fromDateTime(resource.createdAt()));
					statement.setString(3, resource.createdBy());
					statement.setString(4, fromDateTime(resource.modifiedAt()));
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
				return UpdateDatabaseResponse.success(database.getRepoId(), affectedRows);
			}
		} catch(Exception e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("This should not happen", ex);
			}
			log.error("Failed to batch insert resources", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to batch insert resources", e));
		} finally{
			try{
				connection.setAutoCommit(true);
				connection.close();
			} catch(SQLException e){
				log.error("This should never happen", e);
			}
		}
	}
	
	/**
	 * Batch updates a list of resources in the database.
	 *
	 * @param database the database to update the resources in
	 * @param resources the resources to update
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse batchUpdateResources(RepositoryDatabase database, List<Resource> resources) {
		Connection connection = database.getConnection();
		try{
			int affectedRows = 0;
			try(var statement = connection.prepareStatement("UPDATE Resources " +
															"SET last_modified_at = ?, last_modified_by = ?, category = ?" +
															"WHERE resource_path = ?")){
				
				connection.setAutoCommit(false);
				for(var resource : resources){
					statement.setString(1, fromDateTime(resource.modifiedAt()));
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
			return UpdateDatabaseResponse.success(database.getRepoId(), affectedRows);
			
		} catch(Exception e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("Rollback failed", ex);
			}
			log.error("Failed to batch update resources", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to batch update resources", e));
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
	 * Batch deletes a list of resources from the database.
	 *
	 * @param database the database to delete the resources from
	 * @param resourcePaths the list of resource paths to delete
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse batchDeleteResources(RepositoryDatabase database, List<Path> resourcePaths) {
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
			return UpdateDatabaseResponse.success(database.getRepoId(), affectedRows);
			
		} catch(Exception e){
			try{
				connection.rollback();
			} catch(SQLException ex){
				log.error("Rollback failed", ex);
			}
			log.error("Failed to batch delete resources", e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to batch delete resources", e));
		} finally{
			try{
				connection.setAutoCommit(true);
				connection.close();
			} catch(SQLException e){
				log.error("Failed to close connection", e);
			}
		}
	}
	
}

