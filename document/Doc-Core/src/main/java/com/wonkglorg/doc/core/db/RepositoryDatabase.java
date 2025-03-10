package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.db.exception.UserAlreadyExistsException;
import com.wonkglorg.doc.core.db.functions.DatabaseFunctions;
import com.wonkglorg.doc.core.db.functions.ResourceFunctions;
import com.wonkglorg.doc.core.db.functions.UserFunctions;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
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
@SuppressWarnings("UnusedReturnValue")
public class RepositoryDatabase extends SqliteDatabase<HikariDataSource> {
    private static final Logger log = LoggerFactory.getLogger(RepositoryDatabase.class);
    private final RepoProperty repoProperties;
    /**
     * The cache of resources for this database
     */
    private final Map<Path, Resource> resourceCache = new java.util.concurrent.ConcurrentHashMap<>();

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
        hikariConfig.setLeakDetectionThreshold(1000);
        hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Initializes the database for the current repo (creating tables, triggers, etc.)
     */
    @SuppressWarnings("TextBlockBackwardMigration")
    public void initialize() {
        log.info("Initialising Database for repo '{}'", repoProperties.getId());
        try {
            DatabaseFunctions.initializeDatabase(this);
            log.info("Creating triggers");
            DatabaseFunctions.initializeResourceUpdateTrigger(this);
            DatabaseFunctions.initializeResourceDeleteTrigger(this);
        } catch (RuntimeException e) {
            log.error("Error while initializing Database for repo '{}'", repoProperties.getId(), e);
        }

        log.info("Database initialized for repo '{}'", repoProperties.getId());

        ResourceRequest request = new ResourceRequest();


        QueryDatabaseResponse<List<Resource>> resources = ResourceFunctions.getResources(this, request);
        if (resources.isError()) {
            log.error("Error while getting resources for repo '{}'", repoProperties.getId(), resources.getException());
        } else {
            resources.get().forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
        }

    }

    public void shutdown() {
        log.info("Closing Database for repo '{}'", repoProperties.getId());
        try {
            getDataSource().close();
        } catch (RuntimeSQLException e) {
            log.error("Error while closing Database for repo '{}'", repoProperties.getId(), e);
        }
    }

    /**
     * Rebuilds the entire FTS table to remove any unused records
     */
    public void rebuildFts() {
        try {
            DatabaseFunctions.rebuildFts(this);
        } catch (RuntimeSQLException e) {
            log.error("Error while rebuilding fts", e);
        }
    }

    /**
     * Deletes a resource from the database
     *
     * @param resourcePath the path to the resource (should it also delete it from the repo itself?)
     * @return the row count affected, -1 if an error occurred
     */
    public UpdateDatabaseResponse removeResource(Path resourcePath) {
        log.info("Deleting resource {} for repo {}", resourcePath, repoProperties.getId());
        return ResourceFunctions.deleteResource(this, resourcePath);
    }

    /**
     * Gets all resources from this database without its data filled in
     *
     * @return
     * @throws RuntimeSQLException
     */
    public QueryDatabaseResponse<List<Resource>> getResources(ResourceRequest request) {
        log.info("Retrieving resources for repo {}", repoProperties.getId());
        return ResourceFunctions.getResources(this, request);
    }

    /**
     * Searches the database files by their ant path search query and finds any that match the search term
     *
     * @param antPath the ant path search query
     * @return a list of resources that match the search term or an empty list
     */
    public QueryDatabaseResponse<List<Resource>> findByAntPath(String antPath) {
        log.info("Ant path searching resources for repo {}", repoProperties.getId());
        return ResourceFunctions.findByAntPath(this, antPath);
    }

    /**
     * Searches the database files by their content and finds any that match the search term
     *
     * @param searchTerm the term to search for
     * @return a list of resources that match the search term or an empty list
     */
    public QueryDatabaseResponse<List<Resource>> findByContent(String searchTerm) {
        log.info("Searching resources for repo {}", repoProperties.getId());
        return ResourceFunctions.findByContent(this, searchTerm);
    }

    public UpdateDatabaseResponse insertResource(Resource resource) throws RuntimeSQLException {
        log.info("Inserting resource {} for repo {}", resource, repoProperties.getId());
        if (resourceCache.put(resource.resourcePath(), resource) != null) {
            return UpdateDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Resource already exists"));
        }
        return ResourceFunctions.insertResource(this, resource);
    }

    public QueryDatabaseResponse<Boolean> resourceExists(Path path) {
        log.info("Checking if resource {} exists in repo {}", path, repoProperties.getId());
        if (resourceCache.containsKey(path)) {
            return QueryDatabaseResponse.success(this.getRepoId(), true);
        } else {
            return QueryDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Resource does not exist"));
        }
    }

    public UpdateDatabaseResponse updatePath(Path oldPath, Path newPath) {
        log.info("Moving resource '{}' to '{}' in repo '{}'", oldPath, newPath, repoProperties.getId());
        return ResourceFunctions.updatePath(this, oldPath, newPath);
    }

    public QueryDatabaseResponse<Resource> updateResourceData(ResourceUpdateRequest request) {
        log.info("Updating resource '{}' in repo '{}'", request.path, repoProperties.getId());
        return ResourceFunctions.updateResource(this, request);
    }

    public UpdateDatabaseResponse batchInsert(List<Resource> resources) {
        log.info("Batch inserting resources for repo '{}'", repoProperties.getId());
        return ResourceFunctions.batchInsertResources(this, resources);
    }

    public UpdateDatabaseResponse batchUpdate(List<Resource> resources) {
        log.info("Batch updating resources for repo '{}'", repoProperties.getId());
        return ResourceFunctions.batchUpdateResources(this, resources);
    }

    public UpdateDatabaseResponse batchDelete(List<Path> resources) {
        log.info("Batch deleting resources for repo '{}'", repoProperties.getId());
        return ResourceFunctions.batchDeleteResources(this, resources);
    }

    public UpdateDatabaseResponse addUser(UserId userId, String password, String createdBy) {
        log.info("Adding user '{}' in repo '{}'", userId, repoProperties.getId());
        QueryDatabaseResponse<UserProfile> response = UserFunctions.getUser(this, userId);

        if (response.isError()) {
            return UpdateDatabaseResponse.fail(this.getRepoId(), response.getException());
        }

        if (response.get() != null) {
            log.info("Unable to add new user {}, already exists in repo '{}'", userId, repoProperties.getId());
            return UpdateDatabaseResponse.fail(this.getRepoId(), new UserAlreadyExistsException("The user already exists", userId));
        }
        return UserFunctions.addUser(this, userId, password, createdBy);
    }

    public QueryDatabaseResponse<List<UserId>> getUsersFromGroup(GroupId groupId) {
        log.info("Getting users from group '{}' in repO '{}'.", groupId, repoProperties.getId());
        return UserFunctions.getUsersFromGroup(this, groupId);
    }

    public QueryDatabaseResponse<List<GroupId>> getGroupsFromUser(UserId userId) {
        log.info("Getting groups from user '{}' in repo '{}'.", userId, repoProperties.getId());
        return UserFunctions.getGroupsFromUser(this, userId);
    }

    public QueryDatabaseResponse<UserProfile> getUser(UserId userId) {
        log.info("Finding user '{}' in repo '{}'.", userId, repoProperties.getId());
        return UserFunctions.getUser(this, userId);
    }

    public RepoId getRepoId() {
        return repoProperties.getId();
    }

    public RepoProperty getRepoProperties() {
        return repoProperties;
    }
}
