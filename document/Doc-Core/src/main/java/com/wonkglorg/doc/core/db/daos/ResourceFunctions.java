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
import java.sql.Connection;
import java.sql.PreparedStatement;
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
	int deleteResource(RepositoryDatabase database, Path resourcePath) throws RuntimeSQLException {
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
	 * @param connection the connection to the database
	 * @param resource the resource to delete
	 * @return 1 if the resource was deleted, 0 if no resource was deleted, -1 on error
	 * @throws SQLException if there is an error with the database
	 */
	int deleteResource(RepositoryDatabase database, Resource resource) throws RuntimeSQLException {
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
	List<Resource> getResources(RepositoryDatabase database) throws SQLException {
		List<Resource> resources = new ArrayList<>();
		try(ClosingResultSet resultSet = query("""
				Select resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id From Resources
				""").execute(database.getConnection())){
			while(resultSet.next()){
				resources.add(fromResultSet(resultSet));
			}
		} catch(SQLException e){
			log.error("Failed to get resources", e);
		}
		
		return resources;
	}
	
	private Resource fromResultSet(ResultSet resultSet, RepositoryDatabase database) throws SQLException {
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
	@SqlQuery("SELECT resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id FROM Resources WHERE resource_path = :resourcePath")
	Resource findByPath(RepositoryDatabase database, Path resourcePath) throws SQLException {
		try(ClosingResultSet resultSet = query(
				"SELECT resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id FROM Resources WHERE resource_path = :resourcePath").param(
				"resourcePath",
				resourcePath).execute(database.getConnection())){
			while(resultSet.next()){
				return fromResultSet(resultSet);
			}
		} catch(Exception e){
			log.error("Failed to find resource by path", e);
			throw new RuntimeSQLException("Failed to find resource by path", e);
		}
	}
	
	//todo:jmd method not working yet find out why
    /*
                SELECT *
              FROM FileData
			  JOIN Resources
			    ON FileData.resource_path = Resources.resource_path
             WHERE
              CASE
                WHEN length('ab') >= 3
                  THEN data MATCH 'ab'
                ELSE data LIKE '%' || 'ab' || '%'
             END;
     */
	
	/**
	 * Finds all resources with the matching search term in its data
	 *
	 * @param searchTerm the term to search for
	 * @return a list of resources matching the content
	 */
	List<Resource> findByContent(Connection connection, String searchTerm) throws SQLException {
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
		
		try(PreparedStatement statement = connection.prepareStatement(sqlScript)){
			statement.setString(1, searchTerm);
			statement.setString(2, searchTerm);
			statement.setString(3, searchTerm);
			
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				//do stuff
			}
		}
		
		ResultSet resultSet = prepareNamedStatement(sqlScript).param("searchTerm", searchTerm).executeQuery(connection);
		
		while(resultSet.next()){
			//do stuff
		}
	}
	
	/**
	 * Inserts a new resource into the database
	 *
	 * @param resource the resource to add
	 */
	@SqlUpdate("""
			    WITH insert_resource AS (
			        INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by, commit_id)
			        VALUES(:resourcePath, :createdAt, :createdBy, :modifiedAt, :modifiedBy, :commitId)
			        RETURNING resource_path
			    )
			    INSERT INTO FileData(resource_path, data)
			    SELECT resource_path, :data FROM insert_resource;
			""")
	void insertResource(@BindMethods Resource resource) throws Exception; //use bind methods instead of bindBean for records as records are not beans
	
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

