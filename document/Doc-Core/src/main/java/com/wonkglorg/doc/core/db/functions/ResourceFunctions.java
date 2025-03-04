package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.Resource;
import static com.wonkglorg.doc.core.objects.Resource.fromDateTime;
import static com.wonkglorg.doc.core.objects.Resource.parseDateTime;
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
			return UpdateDatabaseResponse.success(database.getRepoId(), i);
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
	public static QueryDatabaseResponse<List<Resource>> getResources(RepositoryDatabase database) {
		List<Resource> resources = new ArrayList<>();
		try(PreparedStatement statement = database.getConnection().prepareStatement(
				"Select resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id From Resources")){
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				resources.add(resourceFromResultSet(resultSet, database));
			}
			return QueryDatabaseResponse.success(database.getRepoId(), resources);
		} catch(SQLException e){
			log.error("Failed to get resources", e);
			return QueryDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException("Failed to get resources", e));
		}
	}
	
	private static Resource resourceFromResultSet(ResultSet resultSet, RepositoryDatabase database) throws SQLException {
		return new Resource(Path.of(resultSet.getString("resource_path")),
				parseDateTime(resultSet.getString("created_at")),
				resultSet.getString("created_by"),
				parseDateTime(resultSet.getString("last_modified_at")),
				resultSet.getString("last_modified_by"),
				database.getRepoProperties().getId(),
				resultSet.getString("commit_id"),
				database.getRepoProperties().isReadOnly(),
				resultSet.getString("category"),
				null);
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
				return QueryDatabaseResponse.success(database.getRepoId(), resourceFromResultSet(resultSet, database));
			}
		} catch(Exception e){
			log.error("Failed to find resource by path", e);
			return QueryDatabaseResponse.fail(database.getRepoId(),
					new RuntimeSQLException("Failed to find resource by path '%s'".formatted(resourcePath), e));
		}
		return QueryDatabaseResponse.success(database.getRepoId(), "No resource matching path %s".formatted(resourcePath), null);
	}
	
	public static QueryDatabaseResponse<List<Resource>> findByCategory(RepositoryDatabase database, String category) {
		return QueryDatabaseResponse.fail(database.getRepoId(), new UnsupportedOperationException("Not implemented yet"));
	}
	
	public static QueryDatabaseResponse<List<Resource>> findByAntPath(RepositoryDatabase database, String antPath) {
		return QueryDatabaseResponse.fail(database.getRepoId(), new UnsupportedOperationException("Not implemented yet"));
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
				resources.add(resourceFromResultSet(resultSet, database));
			}
			
			if(resources.isEmpty()){
				return QueryDatabaseResponse.success(database.getRepoId(),
						"No files found matching search term: '%s'".formatted(searchTerm),
						resources);
			}
			
			return QueryDatabaseResponse.success(database.getRepoId(), resources);
			
		} catch(Exception e){
			log.error("Failed to find resource by content", e);
			return QueryDatabaseResponse.fail(database.getRepoId(),
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
		int affectedRows = 0;
		String sqlResourceInsert = """
				INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category, commit_id)
				VALUES(?, ?, ?, ?, ?, ?, ?)
				""";
		try(PreparedStatement statement = database.getConnection().prepareStatement(sqlResourceInsert)){
			statement.setString(1, resource.resourcePath().toString());
			statement.setString(2, fromDateTime(resource.createdAt()));
			statement.setString(3, resource.createdBy());
			statement.setString(4, fromDateTime(resource.modifiedAt()));
			statement.setString(5, resource.modifiedBy());
			statement.setString(6, resource.category());
			statement.setString(7, resource.commitId());
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
		try(PreparedStatement statement = database.getConnection().prepareStatement(sqlDataInsert)){
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
	 * @param resourcePath the path the resource is at
	 * @param data the data to set it to
	 * @return {@link UpdateDatabaseResponse}
	 */
	public static UpdateDatabaseResponse updateResource(RepositoryDatabase database, Path resourcePath, String data) {
		try(var statement = database.getConnection().prepareStatement("UPDATE FileData SET data = ? WHERE resource_path = ?")){
			statement.setString(1, data);
			statement.setString(2, resourcePath.toString());
			return UpdateDatabaseResponse.success(database.getRepoId(), statement.executeUpdate());
		} catch(Exception e){
			String errorResponse = "Failed to update resource data at path %s".formatted(resourcePath);
			log.error(errorResponse, e);
			return UpdateDatabaseResponse.fail(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
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
					"INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category, commit_id)VALUES(?, ?, ?, ?, ?, ?, ?)")){
				connection.setAutoCommit(false);
				for(var resource : resources){
					statement.setString(1, resource.resourcePath().toString());
					statement.setString(2, fromDateTime(resource.createdAt()));
					statement.setString(3, resource.createdBy());
					statement.setString(4, fromDateTime(resource.modifiedAt()));
					statement.setString(5, resource.modifiedBy());
					statement.setString(6, resource.category());
					statement.setString(7, resource.commitId());
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
			} catch(SQLException e){
				log.error("This should never happen", e);
			}
		}
	}
	
}

