package com.wonkglorg.doc.core.db.daos;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import static com.wonkglorg.doc.core.db.builder.StatementBuilder.query;
import static com.wonkglorg.doc.core.db.builder.StatementBuilder.update;
import com.wonkglorg.doc.core.db.builder.resultset.ClosingResultSet;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.Resource;
import static com.wonkglorg.doc.core.objects.Resource.parseDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All resource related database functions
 */
public class ResourceFunctions{
	private final Logger log = LoggerFactory.getLogger(ResourceFunctions.class);
	
	/**
	 * Deletes a specific resource and all its related data in ResourceData, Tags and Permissions
	 *
	 * @param resourcePath the path to the resource
	 */
	public int deleteResource(RepositoryDatabase database, Path resourcePath) throws RuntimeSQLException {
		try{
			return update("DELETE FROM Resources WHERE resource_path = :resourcePath").param("resourcePath", resourcePath)
																					  .execute(database.getConnection());
		} catch(Exception e){
			log.error("Failed to delete resource", e);
			throw new RuntimeSQLException("Failed to delete resource", e);
		}
	}
	
	/**
	 * Deletes a specific resource and all its related data in ResourceData, Tags and Permissions
	 *
	 * @param database the database to execute the function for
	 * @param resource the resource to delete
	 * @return 1 if the resource was deleted, 0 if no resource was deleted, -1 on error
	 * @throws SQLException if there is an error with the database
	 */
	public int deleteResource(RepositoryDatabase database, Resource resource) throws RuntimeSQLException {
		try{
			return update("DELETE FROM Resources WHERE resource_path = :resourcePath").param("resourcePath", resource.resourcePath())
																					  .execute(database.getConnection());
		} catch(Exception e){
			log.error("Failed to delete resource", e);
			throw new RuntimeSQLException("Failed to delete resource", e);
		}
	}
	
	/**
	 * Retrieves a list of all resources (without its content attached)
	 *
	 * @return a list of resources or an empty list if there are non
	 */
	public List<Resource> getResources(RepositoryDatabase database) throws SQLException {
		List<Resource> resources = new ArrayList<>();
		try(ClosingResultSet resultSet = query("""
				Select resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id From Resources
				""").execute(database.getConnection())){
			while(resultSet.next()){
				resources.add(resourceFromResultSet(resultSet, database));
			}
		} catch(SQLException e){
			log.error("Failed to get resources", e);
		}
		
		return resources;
	}
	
	private Resource resourceFromResultSet(ResultSet resultSet, RepositoryDatabase database) throws SQLException {
		return new Resource(Path.of(resultSet.getString("resource_path")),
				parseDateTime(resultSet.getString("created_at")),
				resultSet.getString("created_by"),
				parseDateTime(resultSet.getString("last_modified_at")),
				resultSet.getString("last_modified_by"),
				database.getRepoProperties().getId(),
				resultSet.getString("commit_id"),
				database.getRepoProperties().isReadOnly(),
				null);
	}
	
	/**
	 * Finds a resource by its fully Qualified path
	 *
	 * @param resourcePath the path to search for
	 * @return the resource found or null
	 */
	public Resource findByPath(RepositoryDatabase database, Path resourcePath) throws SQLException {
		try(ClosingResultSet resultSet = query(
				"SELECT resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id FROM Resources WHERE resource_path = :resourcePath").param(
				"resourcePath",
				resourcePath).execute(database.getConnection())){
			if(resultSet.next()){
				return resourceFromResultSet(resultSet, database);
			}
		} catch(Exception e){
			log.error("Failed to find resource by path", e);
			throw new RuntimeSQLException("Failed to find resource by path", e);
		}
		return null;
	}
	
	/**
	 * Finds all resources with the matching search term in its data
	 *
	 * @param searchTerm the term to search for
	 * @return a list of resources matching the content
	 */
	public List<Resource> findByContent(RepositoryDatabase database, String searchTerm) throws SQLException {
		String sqlScript = """
				SELECT resource_path, data
				  FROM FileData
				 WHERE
				  CASE
				    WHEN length(:searchTerm) >= 3
				      THEN data Match :searchTerm
				    ELSE data LIKE '%' || :searchTerm || '%'
				 END
				""";
		
		try(ClosingResultSet resultSet = query(sqlScript).param("searchTerm", searchTerm).execute(database.getConnection())){
			List<Resource> resources = new ArrayList<>();
			
			while(resultSet.next()){
				resources.add(resourceFromResultSet(resultSet, database));
			}
			return resources;
			
		} catch(Exception e){
			log.error("Failed to find resource by content", e);
			throw new RuntimeSQLException("Failed to find resource by content", e);
		}
		
	}
	
	/**
	 * Inserts a new resource into the database also inserts the data into the FileData table if it was set
	 *
	 * @param resource the resource to add
	 */
	public void insertResource(RepositoryDatabase database, Resource resource) throws Exception {
		
		String sqlResourceInsert = """
				INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by, commit_id)
				VALUES(:resourcePath, :createdAt, :createdBy, :modifiedAt, :modifiedBy, :commitId)
				""";
		try{
			//@formatter:off
			update(sqlResourceInsert)
					.param("resourcePath", resource.resourcePath())
					.param("createdAt", resource.createdAt())
					.param("createdBy", resource.createdBy())
					.param("modifiedAt", resource.modifiedAt())
					.param("modifiedBy", resource.modifiedBy())
					.param("commitId", resource.commitId())
					.execute(database.getConnection());
			//@formatter:on
		} catch(Exception e){
			log.error("Failed to insert resource", e);
			throw new RuntimeSQLException("Failed to insert resource", e);
			
		}
		
		if(resource.data() == null){
			return;
		}
		
		
		String sqlDataInsert = """
				INSERT INTO FileData(resource_path, data)
				VALUES(:resourcePath, :data)
				""";
		try{
			update(sqlDataInsert).param("resourcePath", resource.resourcePath()).param("data", resource.data()).execute(database.getConnection())
		} catch(Exception e){
			log.error("Failed to insert resource", e);
			throw new RuntimeSQLException("Failed to insert resource", e);
			
		}
	}
	
	/**
	 * Updates the path of a resource to the new path (automatically updates all relevant paths duo to triggers generated by {@link DatabaseFunctionsold#setupTriggers()}
	 *
	 * @param oldPath the path to change
	 * @param newPath the path to change it to
	 * @return 1 if the table was changed 0 if no change -1 on error
	 */
	@SqlUpdate("""
			UPDATE Resources
			   SET resource_path = :newPath
			 WHERE resource_path = :oldPath
			""")
	int updatePath(@Bind("oldPath") Path oldPath, @Bind("newPath") Path newPath) throws SQLException;
	
	/**
	 * Updates a resources data
	 *
	 * @param resourcePath the path the resource is at
	 * @param data the data to set it to
	 * @return 1 if the table was changed 0 if no change, -1 on error
	 */
	@SqlUpdate("""
			UPDATE FileData
			   SET data = :data
			 WHERE resource_path = :resourcePath;
			""")
	int updateResource(@Bind("resourcePath") Path resourcePath, @Bind("data") String data) throws SQLException;
	
}

