package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.db.functions.DatabaseFunctions;
import com.wonkglorg.doc.core.db.functions.ResourceFunctions;
import com.wonkglorg.doc.core.db.functions.UserFunctions;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidTagException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.exception.client.TagExistsException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.util.ArrayList;
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
	
	private final UserFunctions userFunctions;
	private final ResourceFunctions resourceFunctions;
	
	/**
	 * The path matcher for this database
	 */
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	/**
	 * The properties of the repository
	 */
	private final RepoProperty repoProperties;
	
	public RepositoryDatabase(RepoProperty repoProperties, Path openInPath) {
		super(getDataSource(openInPath));
		this.repoProperties = repoProperties;
		this.userFunctions = new UserFunctions(this);
		this.resourceFunctions = new ResourceFunctions(this);
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
	public void initialize() throws CoreSqlException {
		log.info("Initialising Database for repo '{}'", repoProperties.getId());
		try{
			DatabaseFunctions.initializeDatabase(this);
			log.info("Creating triggers");
			DatabaseFunctions.initializeTriggers(this);
			//todo:jmd add more triggers for users
		} catch(RuntimeException e){
			log.error("Error while initializing Database for repo '{}'", repoProperties.getId(), e);
		}
		log.info("Database initialized for repo '{}'", repoProperties.getId());
		initializeCaches();
	}
	
	//todo:jmd properly reinitialize the caches
	
	/**
	 * Initializes the caches for the database
	 */
	private void initializeCaches() throws CoreSqlException {
	
	}
	
	/**
	 * Rebuilds the entire FTS table to remove any unused records
	 */
	public void rebuildFts() throws CoreSqlException {
		DatabaseFunctions.rebuildFts(this);
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
		return userCache.containsKey(userId);
	}
	
	//todo:jmd fix path issues each path is treated seperatly. not good
	
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
	 * Updates the path of a resource in the database
	 *
	 * @param oldPath the old path
	 * @param newPath the new path
	 */
	public void updatePath(Path oldPath, Path newPath) throws CoreSqlException {
		log.info("Moving resource '{}' to '{}' in repo '{}'", oldPath, newPath, repoProperties.getId());
		ResourceFunctions.updatePath(this, oldPath, newPath);
		Resource resource = resourceCache.remove(oldPath);
		resource.setResourcePath(newPath);
		resourceCache.put(newPath, resource);
	}
	
	/**
	 * Updates the data of a resource in the database
	 *
	 * @param request the request to update the resource
	 * @return the updated resource
	 */
	public Resource updateResourceData(ResourceUpdateRequest request) throws CoreSqlException {
		log.info("Updating resource '{}' in repo '{}'", request.path(), repoProperties.getId());
		Resource resource = ResourceFunctions.updateResource(this, request);
		resourceCache.put(resource.resourcePath(), resource);
		return resource;
	}

	
	public List<UserProfile> getUsersFromGroup(GroupId groupId) {
		List<UserProfile> profiles = new ArrayList<>();
		groupUsers.get(groupId).forEach(userId -> profiles.add(userCache.get(userId)));
		return profiles;
	}
	
	public List<Group> getGroupsFromUser(UserId userId) {
		List<Group> groups = new ArrayList<>();
		userGroups.get(userId).forEach(groupId -> groups.add(groupCache.get(groupId)));
		return groups;
	}
	
	public List<UserProfile> getAllUsers() {
		log.info("Finding all users in repo '{}'.", repoProperties.getId());
		List<UserProfile> profiles = new ArrayList<>();
		profiles.addAll(userCache.values());
		return profiles;
	}
	
	public RepoId getRepoId() {
		return repoProperties.getId();
	}
	
	public RepoProperty getRepoProperties() {
		return repoProperties;
	}
	
	public UserFunctions userFunctions() {
		return userFunctions;
	}
	
	public ResourceFunctions resourceFunctions() {
		return resourceFunctions;
	}
}
