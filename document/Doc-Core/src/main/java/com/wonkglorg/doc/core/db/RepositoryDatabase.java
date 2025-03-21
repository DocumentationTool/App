package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.db.functions.DatabaseFunctions;
import com.wonkglorg.doc.core.db.functions.ResourceFunctions;
import com.wonkglorg.doc.core.db.functions.UserFunctions;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidTagException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.exception.client.TagExistsException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.interfaces.UserCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
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
public class RepositoryDatabase extends SqliteDatabase<HikariDataSource> implements GroupCalls, UserCalls{
	
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
	private final Map<UserId, UserProfile> userCache = new java.util.concurrent.ConcurrentHashMap<>();
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
		List<Resource> resources = ResourceFunctions.getAllResources(this);
		for(Resource resource : resources){
			resourceCache.put(resource.resourcePath(), resource);
		}
		
		var allTags = ResourceFunctions.getAllTags(this);
		for(Tag tag : allTags){
			tagCache.put(tag.tagId(), tag);
		}
		UserFunctions.getAllUserGroups(this, userGroups, groupUsers);
		
		UserFunctions.getAllUsers(this).forEach(user -> userCache.put(user.getId(), user));
		
		UserFunctions.getAllGroups(this).forEach(group -> groupCache.put(group.getId(), group));
		
		userGroups.forEach((userId, groupIds) -> groupIds.forEach(groupId -> {
			if(groupCache.containsKey(groupId)){
				groupCache.get(groupId).getUserIds().add(userId);
			}
			if(userCache.containsKey(userId)){
				userCache.get(userId).getGroups().add(groupId);
			}
		}));
		
	}
	
	/**
	 * Rebuilds the entire FTS table to remove any unused records
	 */
	public void rebuildFts() throws CoreSqlException {
		DatabaseFunctions.rebuildFts(this);
	}
	
	/**
	 * Deletes a resource from the database
	 *
	 * @param resourcePath the path to the resource (should it also delete it from the repo itself?)
	 * @return the row count affected, -1 if an error occurred
	 */
	public void removeResource(Path resourcePath) throws CoreSqlException {
		log.info("Deleting resource {} for repo {}", resourcePath, repoProperties.getId());
		ResourceFunctions.deleteResource(this, resourcePath);
		resourceCache.remove(resourcePath);
	}
	
	/**
	 * Gets all resources from this database without its data filled in
	 * n
	 */
	public List<Resource> getResources(ResourceRequest request) throws CoreException, InvalidUserException {
		Map<Path, Resource> resources = new HashMap<>(resourceCache);
		
		if(request.getSearchTerm() != null || request.isWithData()){
			Map<Path, String> content = ResourceFunctions.findByContent(this, request);
			
			resources = new HashMap<>();
			for(var entry : resourceCache.entrySet()){
				if(content.containsKey(entry.getKey())){
					resources.put(entry.getKey(), entry.getValue().copy().setData(content.get(entry.getKey())));
				}
			}
		}
		
		// Apply path filtering only if necessary
		if(request.targetPath().isPresent()){
			resources = resources.entrySet().stream().filter(entry -> pathMatcher.match(request.getPath(), entry.getKey().toString())).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		
		resources = resources.entrySet()
							 .stream()
							 .filter(entry -> request.whiteListTags() == null ||
											  request.whiteListTags().isEmpty() ||
											  entry.getValue().hasAnyTagId(request.whiteListTags()))
							 .filter(entry -> request.blacklistTags() == null ||
											  request.blacklistTags().isEmpty() ||
											  !entry.getValue().hasAnyTagId(request.blacklistTags()))
							 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new ArrayList<>(resources.values());
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
	 * Checks if a resource exists in the database
	 *
	 * @param path the path to the resource
	 * @return true if the resource exists false otherwise
	 */
	public boolean resourceExists(Path path) {
		log.info("Checking if resource {} exists in repo {}", path, repoProperties.getId());
		return resourceCache.containsKey(path);
	}
	
	/**
	 * Inserts a resource into the database
	 *
	 * @param resource the resource to insert
	 * @return the row count affected, -1 if an error occurred
	 */
	public void insertResource(Resource resource) throws ClientException, CoreException {
		log.info("Inserting resource {} for repo {}", resource, repoProperties.getId());
		if(resourceExists(resource.resourcePath())){
			throw new ClientException("Resource '%s' in '%s' already exists".formatted(resource.resourcePath(), resource.repoId()));
		}
		ResourceFunctions.insertResource(this, resource);
		resourceCache.put(resource.resourcePath(), resource);
	}
	
	/**
	 * Adds a tag to the database
	 *
	 * @param tag the tag to add
	 */
	public void createTag(Tag tag) throws TagExistsException, CoreSqlException {
		log.info("Adding tag {} for repo {}", tag, repoProperties.getId());
		if(tagExists(tag.tagId())){
			throw new TagExistsException("Tag '%s' already exists".formatted(tag.tagId()));
		}
		ResourceFunctions.addTag(this, tag);
		tagCache.put(tag.tagId(), tag);
	}
	
	/**
	 * Removes a tag from the database
	 *
	 * @param id the id of the tag to remove
	 */
	public void removeTag(TagId id) throws CoreSqlException {
		log.info("Removing tag {} for repo {}", id, repoProperties.getId());
		ResourceFunctions.removeTag(this, id);
		tagCache.remove(id);
		//remove tags from resource cache
		resourceCache.values().forEach(r -> r.getResourceTags().remove(id));
	}
	
	/**
	 * Gets all tags from the database
	 *
	 * @param tagId the tag to get
	 * @return the tags
	 */
	public List<Tag> getTags(TagId tagId) throws InvalidTagException {
		log.info("Getting tag {} for repo {}", tagId, repoProperties.getId());
		if(!tagExists(tagId)){
			throw new InvalidTagException("Tag '%s' does not exist".formatted(tagId));
		}
		return tagCache.values().stream().filter(tag -> tag.tagId().equals(tagId) || tagId == null).collect(Collectors.toList());
	}
	
	/**
	 * Gets all tags from the database
	 *
	 * @return the tags
	 */
	public List<Tag> getTags() {
		return new ArrayList<>(tagCache.values());
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
	
	/**
	 * Batch inserts resources into the database
	 *
	 * @param resources the resources to insert
	 * @return the row count affected, -1 if an error occurred
	 */
	public void batchInsert(List<Resource> resources) throws CoreSqlException {
		log.info("Batch inserting resources for repo '{}'", repoProperties.getId());
		ResourceFunctions.batchInsertResources(this, resources);
		resources.forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
	}
	
	/**
	 * Batch updates resources in the database
	 *
	 * @param resources the resources to update
	 * @return the row count affected, -1 if an error occurred
	 */
	public void batchUpdate(List<Resource> resources) throws CoreSqlException {
		log.info("Batch updating resources for repo '{}'", repoProperties.getId());
		ResourceFunctions.batchUpdateResources(this, resources);
		resources.forEach(resource -> resourceCache.put(resource.resourcePath(), resource));
	}
	
	/**
	 * Batch deletes resources from the database
	 *
	 * @param resources the resources to delete
	 * @return the row count affected, -1 if an error occurred
	 */
	public void batchDelete(List<Path> resources) throws CoreSqlException {
		log.info("Batch deleting resources for repo '{}'", repoProperties.getId());
		ResourceFunctions.batchDeleteResources(this, resources);
		resources.forEach(resourceCache::remove);
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
	 * @param path the path to the file
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
	
	@Override
	public boolean addGroup(RepoId repoId, Group group) throws CoreSqlException {
		log.info("Creating group '{}' in repo '{}'", group.getId(), repoProperties.getId());
		boolean wasInserted = UserFunctions.createGroup(this, group);
		groupCache.put(group.getId(), group);
		return wasInserted;
	}
	
	@Override
	public boolean removeGroup(RepoId repoId, GroupId groupId) {
		boolean wasDeleted = UserFunctions.deleteGroup(this, groupId);
		groupCache.remove(groupId);
		return wasDeleted;
	}
	
	@Override
	public List<Group> getGroups(RepoId repoId, GroupId groupId) {
		if(groupId.isAllGroups()){
			return new ArrayList<>(groupCache.values());
		}
		
		List<Group> groups = new ArrayList<>();
		Group group = groupCache.get(groupId);
		if(group != null){
			groups.add(group);
		}
		return groups;
	}
	
	@Override
	public Group renameGroup(RepoId repoId, GroupId groupId, String newName) throws CoreException, InvalidRepoException, InvalidGroupException {
		UserFunctions.renameGroup(this, groupId, newName);
		Group group = groupCache.get(groupId);
		group.setName(newName);
		return group;
	}
	
	@Override
	public boolean addUserToGroup(RepoId repoId, GroupId groupId, UserId userId)
			throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
		log.info("Adding user '{}' to group '{}' in repo '{}'", userId, groupId, repoProperties.getId());
		boolean isAdded = UserFunctions.addUserToGroup(this, userId, groupId);
		if(isAdded){
			groupUsers.computeIfAbsent(groupId, g -> new ArrayList<>()).add(userId);
			userGroups.computeIfAbsent(userId, u -> new ArrayList<>()).add(groupId);
			if(groupCache.containsKey(groupId)){
				groupCache.get(groupId).getUserIds().add(userId);
			}
			if(userCache.containsKey(userId)){
				userCache.get(userId).getGroups().add(groupId);
			}
		}
		return isAdded;
	}
	
	@Override
	public boolean removeUserFromGroup(RepoId repoId, GroupId groupId, UserId userId)
			throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
		log.info("Removing user '{}' from group '{}' in repo '{}'", userId, groupId, repoProperties.getId());
		boolean isRemoved = UserFunctions.removeUserFromGroup(this, userId, groupId);
		if(isRemoved){
			//update the caches
			if(groupUsers.containsKey(groupId)){
				groupUsers.get(groupId).remove(userId);
			}
			if(userGroups.containsKey(userId)){
				userGroups.get(userId).remove(groupId);
			}
			if(groupCache.containsKey(groupId)){
				groupCache.get(groupId).getUserIds().remove(userId);
			}
			if(userCache.containsKey(userId)){
				userCache.get(userId).getGroups().remove(groupId);
			}
		}
		return isRemoved;
	}
	
	@Override
	public boolean addPermissionToGroup(RepoId repoId, Permission<GroupId> permission) {
		boolean wasAdded = UserFunctions.addGroupPermission(this, permission);
		if(wasAdded){
			if(groupCache.containsKey(permission.id())){
				groupCache.get(permission.id()).getPermissions().put(permission.getPath().toString(), permission);
			}
		}
		return wasAdded;
	}
	
	@Override
	public boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, TargetPath path)
			throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
		boolean removed = UserFunctions.removeGroupPermission(this, groupId, path);
		if(removed){
			if(groupCache.containsKey(groupId)){
				groupCache.get(groupId).getPermissions().remove(path.toString());
			}
		}
		
		return removed;
	}
	
	@Override
	public boolean updatePermissionInGroup(RepoId repoId, Permission<GroupId> permission) throws CoreException, ClientException {
		boolean updated = UserFunctions.updateGroupPermission(this, permission);
		if(updated){
			if(groupCache.containsKey(permission.id())){
				groupCache.get(permission.id()).getPermissions().get(permission.getPath().toString()).setPermission(permission.getPermission());
			}
		}
		return updated;
	}
	
	@Override
	public boolean addUser(RepoId repoId, UserProfile user) throws CoreSqlException {
		log.info("Adding user '{}' in repo '{}'", user.getId(), repoProperties.getId());
		
		boolean isAdded = UserFunctions.addUser(this, user.getId(), user.getPasswordHash(), null);
		
		if(isAdded){
			userCache.put(user.getId(), user);
		}
		return isAdded;
	}
	
	@Override
	public boolean removeUser(RepoId repoId, UserId userId) throws CoreSqlException {
		log.info("Removing user '{}' in repo '{}'.", userId, repoProperties.getId());
		var wasDeleted = UserFunctions.deleteUser(this, userId);
		if(wasDeleted){
			userCache.remove(userId);
		}
		return wasDeleted;
	}
	
	@Override
	public List<UserProfile> getUsers(RepoId repoId, UserId userId) {
		log.info("Finding user '{}' in repo '{}'.", userId, repoProperties.getId());
		if(userId.isAllUsers()){
			return new ArrayList<>(userCache.values());
		}
		
		List<UserProfile> profiles = new ArrayList<>();
		UserProfile profile = userCache.get(userId);
		if(profile != null){
			profiles.add(profile);
		}
		return profiles;
	}
	
	@Override
	public boolean addPermissionToUser(RepoId repoId, Permission<UserId> permission) throws CoreException, ClientException {
		boolean wasAdded = UserFunctions.addUserPermission(this, permission);
		if(wasAdded){
			if(userCache.containsKey(permission.id())){
				userCache.get(permission.id()).getPermissions().put(permission.getPath().toString(), permission);
			}
		}
		return wasAdded;
	}
	
	@Override
	public boolean removePermissionFromUser(RepoId repoId, UserId userId, TargetPath path) throws CoreException, ClientException {
		boolean wasRemoved = UserFunctions.removeUserPermission(this, userId, path);
		if(wasRemoved){
			if(userCache.containsKey(userId)){
				userCache.get(userId).getPermissions().remove(path.toString());
			}
		}
		return wasRemoved;
	}
	
	@Override
	public boolean updatePermissionInUser(RepoId repoId, Permission<UserId> permission) throws CoreException, ClientException {
		boolean wasUpdated = UserFunctions.updateUserPermission(this, permission);
		if(wasUpdated){
			if(userCache.containsKey(permission.id())){
				userCache.get(permission.id()).getPermissions().get(permission.getPath().toString()).setPermission(permission.getPermission());
			}
		}
		return wasUpdated;
	}
	
	public boolean userInGroup(RepoId repoId, GroupId groupId, UserId userId) {
		if(!groupUsers.containsKey(groupId)){
			return false;
		}
		return groupUsers.get(groupId).contains(userId);
	}
	
	public boolean groupHasPermission(GroupId groupId, TargetPath path) {
		if(!groupCache.containsKey(groupId)){
			return false;
		}
		return groupCache.get(groupId).getPermissions().containsKey(path.toString());
	}
	
	public boolean groupHasPermission(GroupId groupId, TargetPath path, PermissionType permission) {
		if(!groupCache.containsKey(groupId)){
			return false;
		}
		Permission<GroupId> groupIdPermission = groupCache.get(groupId).getPermissions().get(path.toString());
		return groupIdPermission != null && groupIdPermission.getPermission().equals(permission);
	}
	
	public boolean userHasPermission(UserId userId, TargetPath path) {
		if(!userCache.containsKey(userId)){
			return false;
		}
		return userCache.get(userId).getPermissions().containsKey(path.toString());
	}
	
	public boolean userHasPermission(UserId userId, TargetPath path, PermissionType permission) {
		if(!userCache.containsKey(userId)){
			return false;
		}
		Permission<UserId> userIdPermission = userCache.get(userId).getPermissions().get(path.toString());
		return userIdPermission != null && userIdPermission.getPermission().equals(permission);
	}
}
