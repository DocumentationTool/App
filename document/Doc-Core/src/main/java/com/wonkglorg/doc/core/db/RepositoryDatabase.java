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
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the database object for a defined repository
 */
@SuppressWarnings("UnusedReturnValue")
public class RepositoryDatabase extends SqliteDatabase<HikariDataSource>{
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
		try{
			DatabaseFunctions.initializeDatabase(this);
			log.info("Creating triggers");
			DatabaseFunctions.initializeResourceUpdateTrigger(this);
			DatabaseFunctions.initializeResourceDeleteTrigger(this);
		} catch(RuntimeException e){
			log.error("Error while initializing Database for repo '{}'", repoProperties.getId(), e);
		}
		
		log.info("Database initialized for repo '{}'", repoProperties.getId());
		
		QueryDatabaseResponse<List<Resource>> resources = ResourceFunctions.getAllResources(this);
		if(resources.isError()){
			log.error("Error while getting resources for repo '{}'", repoProperties.getId(), resources.getException());
		} else {
			log.info("Caching {} resources for repo '{}'", resources.get().size(), repoProperties.getId());
			resources.get().forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
			//todo:jmd fill user cache, tag cache and group cache
		}
		
		ResourceFunctions.getAllTags(this).forEach(tag -> tagCache.put(tag.tagId(), tag));
		QueryDatabaseResponse<List<UserProfile>> allUsers = UserFunctions.getAllUsers(this);
		if(allUsers.isError()){
			log.error("Error while getting users for repo '{}'", repoProperties.getId(), allUsers.getException());
		}
		allUsers.get().forEach(user -> userProfiles.put(user.getId(), user));
		
	}
	
	/**
	 * Rebuilds the entire FTS table to remove any unused records
	 */
	public void rebuildFts() {
		try{
			DatabaseFunctions.rebuildFts(this);
		} catch(RuntimeSQLException e){
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
		if(updateDatabaseResponse.isSuccess()){
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
		
		Map<Path, Resource> resources = resourceCache; //start with the full cache
		
		if(request.searchTerm != null || request.withData){
			QueryDatabaseResponse<Map<Path, String>> byContent = ResourceFunctions.findByContent(this, request);
			if(byContent.isError()){
				return QueryDatabaseResponse.fail(this.getRepoId(), byContent.getException());
			}
			
			resources = new HashMap<>();
			for(var entry : resourceCache.entrySet()){
				if(byContent.get().containsKey(entry.getKey())){
					resources.put(entry.getKey(), entry.getValue().copy().setData(byContent.get().get(entry.getKey())));
				}
			}
		}
		
		// Apply path filtering only if necessary
		if(request.path != null){
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
		Collection<Resource> resourcesToReturn = resources.values();
		if(request.userId == null){
			return QueryDatabaseResponse.success(this.getRepoId(), resourcesToReturn);
		}
		
		UserId userId = new UserId(request.userId);
		
		if(!userExists(userId)){
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
		if(userId == null){
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
		if(groupId == null){
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
		if(tag == null){
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
		if(tag == null){
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
		if(resourceCache.containsKey(path)){
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
		if(resourceExists(resource.resourcePath()).get()){
			return UpdateDatabaseResponse.fail(this.getRepoId(), new RuntimeException("Resource already exists"));
		}
		resourceCache.put(resource.resourcePath(), resource);
		return ResourceFunctions.insertResource(this, resource);
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
		if(updateDatabaseResponse.isSuccess()){
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
		if(updateDatabaseResponse.isSuccess()){
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
		if(updateDatabaseResponse.isSuccess()){
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
		if(updateDatabaseResponse.isSuccess()){
			resources.forEach(resourceCache::remove);
		}
		return updateDatabaseResponse;
	}
	
	public UpdateDatabaseResponse addUser(UserId userId, String password, String createdBy) {
		log.info("Adding user '{}' in repo '{}'", userId, repoProperties.getId());
		QueryDatabaseResponse<UserProfile> response = UserFunctions.getUser(this, userId);
		
		if(response.isError()){
			return UpdateDatabaseResponse.fail(this.getRepoId(), response.getException());
		}
		
		if(response.get() != null){
			log.info("Unable to add new user {}, already exists in repo '{}'", userId, repoProperties.getId());
			return UpdateDatabaseResponse.fail(this.getRepoId(), new UserAlreadyExistsException("The user already exists", userId));
		}
		return UserFunctions.addUser(this, userId, password, createdBy);
	}
	
	public List<UserId> getUsersFromGroup(GroupId groupId) {
		return groupUsers.get(groupId);
	}
	
	public List<GroupId> getGroupsFromUser(UserId userId) {
		return userGroups.get(userId);
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
	
	public Map<TagId, Tag> getTagCache() {
		return tagCache;
	}
}
