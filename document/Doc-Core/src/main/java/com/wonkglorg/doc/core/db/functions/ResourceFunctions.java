package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.builder.resultset.ClosingResultSet;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.wonkglorg.doc.core.db.builder.StatementBuilder.query;
import static com.wonkglorg.doc.core.db.builder.StatementBuilder.update;
import static com.wonkglorg.doc.core.objects.Resource.fromDateTime;
import static com.wonkglorg.doc.core.objects.Resource.parseDateTime;

/**
 * All resource related database functions
 */
public class ResourceFunctions {
    private static final Logger log = LoggerFactory.getLogger(ResourceFunctions.class);

    /**
     * Deletes a specific resource and all its related data in ResourceData, Tags and Permissions
     *
     * @param resourcePath the path to the resource
     */
    public static UpdateDatabaseResponse deleteResource(RepositoryDatabase database, Path resourcePath) {
        try {
            Integer i = update("DELETE FROM Resources WHERE resource_path = :resourcePath").param("resourcePath", resourcePath).execute(database.getConnection());
            return UpdateDatabaseResponse.success(i);
        } catch (Exception e) {
            log.error("Failed to delete resource", e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException("Failed to delete resource", e));
        }
    }

    /**
     * Retrieves a list of all resources (without its content attached)
     *
     * @return a list of resources or an empty list if there are non
     */
    public static QueryDatabaseResponse<List<Resource>> getResources(RepositoryDatabase database) throws SQLException {
        List<Resource> resources = new ArrayList<>();
        try (ClosingResultSet resultSet = query("""
                Select resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id From Resources
                """).execute(database.getConnection())) {
            while (resultSet.next()) {
                resources.add(resourceFromResultSet(resultSet, database));
            }
            return QueryDatabaseResponse.success(resources);
        } catch (SQLException e) {
            log.error("Failed to get resources", e);
            return QueryDatabaseResponse.fail(new RuntimeSQLException("Failed to get resources", e));
        }
    }

    private static Resource resourceFromResultSet(ResultSet resultSet, RepositoryDatabase database) throws SQLException {
        return new Resource(Path.of(resultSet.getString("resource_path")), parseDateTime(resultSet.getString("created_at")), resultSet.getString("created_by"), parseDateTime(resultSet.getString("last_modified_at")), resultSet.getString("last_modified_by"), database.getRepoProperties().getId(), resultSet.getString("commit_id"), database.getRepoProperties().isReadOnly(), null);
    }

    /**
     * Finds a resource by its fully Qualified path
     *
     * @param resourcePath the path to search for
     * @return the resource found or null
     */
    public static QueryDatabaseResponse<Resource> findByPath(RepositoryDatabase database, Path resourcePath) {
        try (ClosingResultSet resultSet = query("SELECT resource_path,created_at,created_by,last_modified_at,last_modified_by,category,commit_id FROM Resources WHERE resource_path = :resourcePath").param("resourcePath", resourcePath).execute(database.getConnection())) {
            if (resultSet.next()) {
                return QueryDatabaseResponse.success(resourceFromResultSet(resultSet, database));
            }
        } catch (Exception e) {
            log.error("Failed to find resource by path", e);
            return QueryDatabaseResponse.fail(new RuntimeSQLException("Failed to find resource by path '%s'".formatted(resourcePath), e));
        }
        return QueryDatabaseResponse.success("No resource matching path %s".formatted(resourcePath), null);
    }

    /**
     * Finds all resources with the matching search term in its data
     *
     * @param searchTerm the term to search for
     * @return a list of resources matching the content
     */
    public static QueryDatabaseResponse<List<Resource>> findByContent(RepositoryDatabase database, String searchTerm) {
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

        try (ClosingResultSet resultSet = query(sqlScript).param("searchTerm", searchTerm).execute(database.getConnection())) {
            List<Resource> resources = new ArrayList<>();

            while (resultSet.next()) {
                resources.add(resourceFromResultSet(resultSet, database));
            }

            if (resources.isEmpty()) {
                return QueryDatabaseResponse.success("No files found matching search term: '%s'".formatted(searchTerm), resources);
            }

            return QueryDatabaseResponse.success(resources);

        } catch (Exception e) {
            log.error("Failed to find resource by content", e);
            return QueryDatabaseResponse.fail(new RuntimeSQLException("Failed to find resource by text '%s'".formatted(searchTerm), e));
        }

    }

    /**
     * Inserts a new resource into the database also inserts the data into the FileData table if it was set
     *
     * @param resource the resource to add
     */
    public static UpdateDatabaseResponse insertResource(RepositoryDatabase database, Resource resource) throws Exception {
        int affectedRows = 0;
        String sqlResourceInsert = """
                INSERT INTO Resources(resource_path, created_at, created_by, last_modified_at, last_modified_by,category, commit_id)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = database.getConnection().prepareStatement(sqlResourceInsert)) {
            statement.setString(1, resource.resourcePath().toString());
            statement.setString(2, fromDateTime(resource.createdAt()));
            statement.setString(3, resource.createdBy());
            statement.setString(4, fromDateTime(resource.modifiedAt()));
            statement.setString(5, resource.modifiedBy());
            statement.setString(6, resource.category());
            statement.setString(7, resource.commitId());
            affectedRows = statement.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to insert resource", e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException("Failed to insert resource", e));
        }

        if (resource.data() == null) { //no data to insert so we skip the next part
            return UpdateDatabaseResponse.success(affectedRows);
        }

        String sqlDataInsert = """
                INSERT INTO FileData(resource_path, data)
                VALUES(?, ?)
                """;
        try (PreparedStatement statement = database.getConnection().prepareStatement(sqlDataInsert)) {
            statement.setString(1, resource.resourcePath().toString());
            statement.setString(2, resource.data());
            affectedRows += statement.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to insert resource data", e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException("Failed to insert resource data", e));
        }
        return UpdateDatabaseResponse.success(affectedRows);
    }

    /**
     * Updates the path of a resource to the new path (automatically updates all relevant paths duo to triggers generated by {@link DatabaseFunctions}
     *
     * @param oldPath the path to change
     * @param newPath the path to change it to
     * @return 1 if the table was changed 0 if no change -1 on error
     */
    public static UpdateDatabaseResponse updatePath(RepositoryDatabase database, Path oldPath, Path newPath) {
        log.info("Updating resource path '{}' to '{}'", oldPath, newPath);
        try (PreparedStatement statement = database.getConnection().prepareStatement("UPDATE Resources SET resource_path = ? WHERE resource_path = ?")) {
            statement.setString(1, newPath.toString());
            statement.setString(2, oldPath.toString());
            return UpdateDatabaseResponse.success(statement.executeUpdate());
        } catch (Exception e) {
            String errorResponse = "Failed to update resource path from '%s' to '%s'".formatted(oldPath, newPath);
            log.error(errorResponse, e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }

    /**
     * Updates a resources data
     *
     * @param resourcePath the path the resource is at
     * @param data         the data to set it to
     * @return 1 if the table was changed 0 if no change, -1 on error
     */
    public static UpdateDatabaseResponse updateResource(RepositoryDatabase database, Path resourcePath, String data) {
        try (var statement = database.getConnection().prepareStatement("UPDATE FileData SET data = ? WHERE resource_path = ?")) {
            statement.setString(1, data);
            statement.setString(2, resourcePath.toString());
            return UpdateDatabaseResponse.success(statement.executeUpdate());
        } catch (Exception e) {
            String errorResponse = "Failed to update resource data at path %s".formatted(resourcePath);
            log.error(errorResponse, e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }

}

