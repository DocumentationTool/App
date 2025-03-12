package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.db.exception.UserAlreadyExistsException;
import com.wonkglorg.doc.core.db.functions.DatabaseFunctions;
import com.wonkglorg.doc.core.db.functions.ResourceFunctions;
import com.wonkglorg.doc.core.db.functions.UserFunctions;
import com.wonkglorg.doc.core.objects.*;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the database object for a defined repository
 */
@SuppressWarnings("UnusedReturnValue")
public class RepositoryDatabase extends SqliteDatabase<HikariDataSource> {
    private static final Logger log = LoggerFactory.getLogger(RepositoryDatabase.class);
    /**
     * The path matcher for this database
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    /**
     * The properties of the repository
     */
    private final RepoProperty repoProperties;
    /**
     * The cache of resources for this database
     */
    private final Map<Path, Resource> resourceCache = new java.util.concurrent.ConcurrentHashMap<>();
    /**
     * The cache of user profiles for this database
     */
    private final Map<UserId, UserProfile> userProfiles = new java.util.concurrent.ConcurrentHashMap<>();
    /**
     * Helper map to quickly access connections between groups and users
     */
    private final Map<GroupId, List<UserId>> groupUsers = new java.util.concurrent.ConcurrentHashMap<>();
    /**
     * Helper map to quickly access connections between users and groups
     */
    private final Map<UserId, List<GroupId>> userGroups = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * The cache of groups for this database
     */
    private final Map<GroupId, Group> groupCache = new java.util.concurrent.ConcurrentHashMap<>();
    /**
     * The cache of tags for this database
     */
    private final Map<TagId, Tag> tagCache = new HashMap<>();

    /**
     * Keeps track of currently edited files todo add a way to every now and then confirm if its still edited
     */
    private final Map<UserId, Path> currentlyEdited = new HashMap<>();

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
    public void initialize() {
        log.info("Initialising Database for repo '{}'", repoProperties.getId());
        try {
            DatabaseFunctions.initializeDatabase(this);
            log.info("Creating triggers");
            DatabaseFunctions.initializeResourceUpdateTrigger(this);
            DatabaseFunctions.initializeResourceDeleteTrigger(this);
            DatabaseFunctions.initializeUserDeleteTrigger(this);
        } catch (RuntimeException e) {
            log.error("Error while initializing Database for repo '{}'", repoProperties.getId(), e);
        }
        log.info("Database initialized for repo '{}'", repoProperties.getId());
        initializeCaches();
    }



    //todo:jmd properly reinitialize the caches
    /**
     * Initializes the caches for the database
     */
    private void initializeCaches() {
        QueryDatabaseResponse<List<Resource>> resources = ResourceFunctions.getAllResources(this);
        if (resources.isError()) {
            log.error("Error while getting resources for repo '{}'", repoProperties.getId(), resources.getException());
        } else {
            log.info("Caching {} resources for repo '{}'", resources.get().size(), repoProperties.getId());
            resources.get().forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
        }

        var allTags = ResourceFunctions.getAllTags(this);

        if (allTags.isError()) {
            log.error("Error while getting tags for repo '{}'", repoProperties.getId(), allTags.getException());
        } else {
            log.info("Cache {} tags for repo '{}'", allTags.get().size(), repoProperties.getId());
            allTags.get().forEach(tag -> tagCache.put(tag.tagId(), tag));
        }

        QueryDatabaseResponse<List<UserProfile>> allUsers = UserFunctions.getAllUsers(this);

        if (allUsers.isError()) {
            log.error("Error while getting users for repo '{}'", repoProperties.getId(), allUsers.getException());
        } else {
            log.info("Cache {} users for repo '{}'", allUsers.get().size(), repoProperties.getId());
            allUsers.get().forEach(user -> userProfiles.put(user.getId(), user));
        }

        UserFunctions.getAllUserGroups(this, userGroups, groupUsers);

        var allGroups = UserFunctions.getAllGroups(this);
        if (allGroups.isError()) {
            log.error("Error while getting groups for repo '{}'", repoProperties.getId(), allGroups.getException());
        } else {
            log.info("Cache {} groups for repo '{}'", allGroups.get().size(), repoProperties.getId());
            allGroups.get().forEach(group -> groupCache.put(group.getId(), group));
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
        UpdateDatabaseResponse updateDatabaseResponse = ResourceFunctions.deleteResource(this, resourcePath);
        if (updateDatabaseResponse.isSuccess()) {
            resourceCache.remove(resourcePath);
        }
        return updateDatabaseResponse;
    }

    /**
     * Gets all resources from this database without its data filled in
     *
     * @return
     * @throws RuntimeSQLException
     */
    public QueryDatabaseResponse<Collection<Resource>> getResources(ResourceRequest request) {
        log.info("Retrieving resources for repo {}", repoProperties.getId());

        Map<Path, Resource> resources = new HashMap<>(resourceCache); //start with the full cache

        if (request.searchTerm != null || request.withData) {
            QueryDatabaseResponse<Map<Path, String>> byContent = ResourceFunctions.findByContent(this, request);
            if (byContent.isError()) {
                return QueryDatabaseResponse.fail(this.getRepoId(), byContent.getException());
            }

            resources = new HashMap<>();
            for (var entry : resourceCache.entrySet()) {
                if (byContent.get().containsKey(entry.getKey())) {
                    resources.put(entry.getKey(), entry.getValue().copy().setData(byContent.get().get(entry.getKey())));
                }
            }
        }

        // Apply path filtering only if necessary
        if (request.path != null) {
            resources = resources.entrySet()
                    .stream()
                    .filter(entry -> pathMatcher.match(request.path, entry.getKey().toString()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        resources = resources.entrySet()
                .stream()
                .filter(entry -> request.whiteListTags.isEmpty() ||
                        entry.getValue().hasAnyTag(request.whiteListTags))
                .filter(entry -> request.blacklistTags.isEmpty() || !entry.getValue().hasAnyTag(request.blacklistTags))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Handle user-specific filtering
        Collection<Resource> resourcesToReturn = new ArrayList<>(resources.values());
        if (request.userId == null) {
            return QueryDatabaseResponse.success(this.getRepoId(), resourcesToReturn);
        }

        UserId userId = new UserId(request.userId);

        if (!userExists(userId)) {
            return QueryDatabaseResponse.fail(this.getRepoId(), new RuntimeException("User does not exist"));
        }
        resourcesToReturn = userProfiles.get(userId).getAllowedResources(resourcesToReturn);

        return QueryDatabaseResponse.success(this.getRepoId(), resourcesToReturn);
    }

    /**
     * Checks if a user exists in the database
     *
     * @param userId the user to check
     * @return weather it exists or not
     */
    public boolean userExists(UserId userId) {
        if (userId == null) {
            return false;
        }
        return userProfiles.containsKey(userId);
    }

    /**
     * Checks if a group exists in the database
     *
     * @param groupId the group to check
     * @return weather it exists or not
     */
    public boolean groupExists(GroupId groupId) {
        if (groupId == null) {
            return false;
        }
        return groupCache.containsKey(groupId);
    }

    /**
     * Checks if a tag exists in the database
     *
     * @param tag the tag to check
     * @return weather it exists or not
     */
    public boolean tagExists(TagId tag) {
        if (tag == null) {
            return false;
        }
        return tagCache.containsKey(tag);
    }

    /**
     * Checks if a tag exists in the database
     *
     * @param tag the tag to check
     * @return weather it exists or not
     */
    public boolean tagExists(Tag tag) {
        if (tag == null) {
            return false;
        }
        Tag returnTag = tagCache.get(tag.tagId());
        return returnTag != null && returnTag.equals(tag);
    }

    /**
     * Checks if a resource exists in the database
     *
     * @param path the path to the resource
     * @return true if the resource exists false otherwise
     */
    public QueryDatabaseResponse<Boolean> resourceExists(Path path) {
        log.info("Checking if resource {} exists in repo {}", path, repoProperties.getId());
        if (resourceCache.containsKey(path)) {
            return QueryDatabaseResponse.success(this.getRepoId(), true);
        } else {
            return QueryDatabaseResponse.success(this.getRepoId(), false);
        }
    }

    /**
     * Inserts a resource into the database
     *
     * @param resource the resource to insert
     * @return the row count affected, -1 if an error occurred
     */
    public UpdateDatabaseResponse insertResource(Resource resource) throws RuntimeSQLException {
        log.info("Inserting resource {} for repo {}", resource, repoProperties.getId());
        if (resourceExists(resource.resourcePath()).get()) {
            return UpdateDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Resource already exists"));
        }
        resourceCache.put(resource.resourcePath(), resource);
        return ResourceFunctions.insertResource(this, resource);
    }


    /**
     * Adds a tag to the database
     *
     * @param tag the tag to add
     */
    public UpdateDatabaseResponse addTag(Tag tag) {
        log.info("Adding tag {} for repo {}", tag, repoProperties.getId());
        if (tagExists(tag.tagId())) {
            return UpdateDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Tag already exists"));
        }
        tagCache.put(tag.tagId(), tag);
        return ResourceFunctions.addTag(this, tag);
    }

    /**
     * Removes a tag from the database
     *
     * @param id the id of the tag to remove
     */
    public UpdateDatabaseResponse removeTag(TagId id) {
        log.info("Removing tag {} for repo {}", id, repoProperties.getId());
        if (!tagExists(id)) {
            return UpdateDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Tag does not exist"));
        }
        tagCache.remove(id);
        //todo:jmd how to properly update the cache for resources? reget it entirely?
        return ResourceFunctions.removeTag(this, id);
    }


    /**
     * Gets all tags from the database
     *
     * @param tagId the tag to get
     * @return the tags
     */
    public QueryDatabaseResponse<List<Tag>> getTags(TagId tagId) {
        log.info("Getting tag {} for repo {}", tagId, repoProperties.getId());
        if (!tagExists(tagId)) {
            return QueryDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Tag does not exist"));
        }
        return QueryDatabaseResponse.success(this.getRepoId(), tagCache.values().stream().filter(tag -> tag.tagId().equals(tagId) || tagId == null).collect(Collectors.toList()));
    }


    /**
     * Updates the path of a resource in the database
     *
     * @param oldPath the old path
     * @param newPath the new path
     * @return the row count affected, -1 if an error occurred
     */
    public UpdateDatabaseResponse updatePath(Path oldPath, Path newPath) {
        log.info("Moving resource '{}' to '{}' in repo '{}'", oldPath, newPath, repoProperties.getId());
        UpdateDatabaseResponse updateDatabaseResponse = ResourceFunctions.updatePath(this, oldPath, newPath);
        if (updateDatabaseResponse.isSuccess()) {
            Resource resource = resourceCache.remove(oldPath);
            resource.setResourcePath(newPath);
            resourceCache.put(newPath, resource);
        }
        return updateDatabaseResponse;
    }

    /**
     * Updates the data of a resource in the database
     *
     * @param request the request to update the resource
     * @return the updated resource
     */
    public QueryDatabaseResponse<Resource> updateResourceData(ResourceUpdateRequest request) {
        log.info("Updating resource '{}' in repo '{}'", request.path, repoProperties.getId());
        return ResourceFunctions.updateResource(this, request);
    }

    /**
     * Batch inserts resources into the database
     *
     * @param resources the resources to insert
     * @return the row count affected, -1 if an error occurred
     */
    public UpdateDatabaseResponse batchInsert(List<Resource> resources) {
        log.info("Batch inserting resources for repo '{}'", repoProperties.getId());
        UpdateDatabaseResponse updateDatabaseResponse = ResourceFunctions.batchInsertResources(this, resources);
        if (updateDatabaseResponse.isSuccess()) {
            resources.forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
        }
        return updateDatabaseResponse;
    }

    /**
     * Batch updates resources in the database
     *
     * @param resources the resources to update
     * @return the row count affected, -1 if an error occurred
     */
    public UpdateDatabaseResponse batchUpdate(List<Resource> resources) {
        log.info("Batch updating resources for repo '{}'", repoProperties.getId());
        UpdateDatabaseResponse updateDatabaseResponse = ResourceFunctions.batchUpdateResources(this, resources);
        if (updateDatabaseResponse.isSuccess()) {
            resources.forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
        }
        return updateDatabaseResponse;
    }

    /**
     * Batch deletes resources from the database
     *
     * @param resources the resources to delete
     * @return the row count affected, -1 if an error occurred
     */
    public UpdateDatabaseResponse batchDelete(List<Path> resources) {
        log.info("Batch deleting resources for repo '{}'", repoProperties.getId());
        UpdateDatabaseResponse updateDatabaseResponse = ResourceFunctions.batchDeleteResources(this, resources);
        if (updateDatabaseResponse.isSuccess()) {
            resources.forEach(resourceCache::remove);
        }
        return updateDatabaseResponse;
    }

    public UpdateDatabaseResponse addUser(UserId userId, String password) {
        log.info("Adding user '{}' in repo '{}'", userId, repoProperties.getId());
        if (userExists(userId)) {
            return UpdateDatabaseResponse.fail(this.getRepoId(), new UserAlreadyExistsException("The user already exists", userId));
        }
        UpdateDatabaseResponse updateDatabaseResponse = UserFunctions.addUser(this, userId, password, null);

        if (updateDatabaseResponse.isSuccess()) {
            userProfiles.put(userId, new UserProfile(userId, password, new HashSet<>(), new HashSet<>()));
        }
        return updateDatabaseResponse;
    }

    public List<UserId> getUsersFromGroup(GroupId groupId) {
        return groupUsers.get(groupId);
    }

    public List<GroupId> getGroupsFromUser(UserId userId) {
        return userGroups.get(userId);
    }

    public List<UserProfile> getUsers(UserId userId) {
        log.info("Finding user '{}' in repo '{}'.", userId, repoProperties.getId());
        if (userId == null) {
            return new ArrayList<>(userProfiles.values());
        }

        return List.of(userProfiles.get(userId));
    }

    public void removeUser(UserId userId) {
        log.info("Removing user '{}' in repo '{}'.", userId, repoProperties.getId());
        UserFunctions.remove(this, userId);
        userProfiles.remove(userId);
    }

    public RepoId getRepoId() {
        return repoProperties.getId();
    }

    public RepoProperty getRepoProperties() {
        return repoProperties;
    }

    public Map<TagId, Tag> getTagCache() {
        return tagCache;
    }

    /**
     * Check if a file is currently being edited
     *
     * @param path the path to check
     * @return the user editing the file or null if no one is editing it
     */
    public UserId isBeingEdited(Path path) {
        return currentlyEdited.entrySet().stream().filter(p -> p.getValue().equals(path)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /**
     * Check if a user is currently editing a file
     *
     * @param userId the user to check
     * @return true if they are editing, false otherwise
     */
    public boolean isUserEditing(UserId userId) {
        return currentlyEdited.containsKey(userId);
    }

    /**
     * Sets a user as editing a file locking it for others to edit at the same time
     *
     * @param userId the user editing
     * @param path   the path to the file
     * @return true if the file is now being edited, false otherwise
     */
    public void setCurrentlyEdited(UserId userId, Path path) {
        currentlyEdited.put(userId, path);
    }

    /**
     * Removes a user from editing a file
     *
     * @param userId the user to remove
     */
    public void removeCurrentlyEdited(UserId userId) {
        currentlyEdited.remove(userId);
    }

    /**
     * Removes a file from being edited
     *
     * @param path the path to the file
     */
    public void removeCurrentlyEdited(Path path) {
        currentlyEdited.entrySet().removeIf(entry -> entry.getValue().equals(path));
    }

}
