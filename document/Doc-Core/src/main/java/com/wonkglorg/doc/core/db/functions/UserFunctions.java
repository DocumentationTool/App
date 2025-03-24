package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.interfaces.UserCalls;
import com.wonkglorg.doc.core.objects.DateHelper;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserFunctions implements IDBFunctions, UserCalls, GroupCalls{
	private static final Logger log = LoggerFactory.getLogger(UserFunctions.class);
	
	private final RepositoryDatabase database;
	
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
	
	public UserFunctions(RepositoryDatabase database) {
		this.database = database;
	}
	
	@Override
	public void initialize() {
		log.info("Initializing cache for user functions in repo '{}'", database.getRepoProperties().getId());
		Connection connection = database.getConnection();
		try{
			getAllUserGroups(connection, userGroups, groupUsers);
			
			getAllUsers(connection).forEach(user -> userCache.put(user.getId(), user));
			
			getAllGroups(connection).forEach(group -> groupCache.put(group.getId(), group));
			
			userGroups.forEach((userId, groupIds) -> groupIds.forEach(groupId -> {
				if(groupCache.containsKey(groupId)){
					groupCache.get(groupId).getUserIds().add(userId);
				}
				if(userCache.containsKey(userId)){
					userCache.get(userId).getGroups().add(groupId);
				}
			}));
			log.info("Initialized user functions for repo '{}'", database.getRepoProperties().getId());
		} catch(Exception e){
			log.error("Failed to initialize user functions", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public Set<GroupId> getGroupsFromUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT group_id FROM UserGroups WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				Set<GroupId> groups = new HashSet<>();
				while(rs.next()){
					groups.add(GroupId.of(rs.getString("group_id")));
				}
				return groups;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get groups from user";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	private void getAllUserGroups(Connection connection, Map<UserId, List<GroupId>> userGroups, Map<GroupId, List<UserId>> userIds) {
		try(var statement = connection.prepareStatement("SELECT * FROM UserGroups")){
			try(var rs = statement.executeQuery()){
				while(rs.next()){
					UserId userId = UserId.of(rs.getString("user_id"));
					GroupId groupId = GroupId.of(rs.getString("group_id"));
					userGroups.computeIfAbsent(userId, k -> new ArrayList<>()).add(groupId);
					userIds.computeIfAbsent(groupId, k -> new ArrayList<>()).add(userId);
				}
			}
		} catch(Exception e){
			log.error("Failed to get all user groups", e);
		}
	}
	
	private List<Group> getAllGroups(Connection connection) throws CoreSqlException {
		try(var statement = connection.prepareStatement("SELECT * FROM Groups")){
			try(var rs = statement.executeQuery()){
				List<Group> groups = new ArrayList<>();
				while(rs.next()){
					groups.add(new Group(GroupId.of(rs.getString("group_id")),
							rs.getString("group_name"),
							rs.getString("created_by"),
							rs.getString("created_at"),
							rs.getString("last_modified_by"),
							rs.getString("last_modified_at")
					
					));
				}
				for(Group group : groups){
					List<Permission<GroupId>> permissionsForGroup = PermissionFunctions.getPermissionsForGroup(database, connection, group.getId());
					for(Permission<GroupId> permission : permissionsForGroup){
						group.getPermissions().put(permission.getPath().toString(), permission);
					}
					
				}
				
				return groups;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get all groups";
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	private List<UserProfile> getAllUsers(Connection connection) throws CoreSqlException {
		try(var statement = connection.prepareStatement("SELECT * FROM Users")){
			try(var rs = statement.executeQuery()){
				List<UserProfile> users = new ArrayList<>();
				while(rs.next()){
					String userId = rs.getString("user_id");
					String passwordHash = rs.getString("password_hash");
					Map<String, Permission<UserId>> userPermissions = new HashMap<>();
					for(var permission : PermissionFunctions.getPermissionsForUser(database, connection, UserId.of(userId))){
						userPermissions.put(permission.getPath().toString(), permission);
					}
					
					var userRoles = PermissionFunctions.getRolesForUser(database, UserId.of(userId));
					var groups = getGroupsFromUser(database, UserId.of(userId));
					users.add(new UserProfile(UserId.of(userId), passwordHash, userPermissions, userRoles, groups));
				}
				return users;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get all users";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	@Override
	public boolean addUser(RepoId repoId, UserProfile user) throws CoreSqlException {
		log.info("Adding user '{}' to repo '{}'", user.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(?,?,?,?)")){
			statement.setString(1, user.getId().id());
			statement.setString(2, user.getPasswordHash());
			statement.setString(3, "system");
			statement.setString(4, "system");
			statement.executeUpdate();
			userCache.put(user.getId(), user);
			log.info("User '{}' added to repo '{}'", user.getId(), repoId.id());
			return true;
		} catch(Exception e){
			throw new CoreSqlException("Failed to add user", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean removeUser(RepoId repoId, UserId userId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM Users WHERE user_id = ?")){
			statement.setString(1, userId.id());
			userCache.remove(userId);
			return statement.executeUpdate() > 0;
		} catch(Exception e){
			String errorResponse = "Failed to delete user '%s' in '%s'".formatted(userId, database.getRepoId());
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public List<UserProfile> getUsers(RepoId repoId, UserId userId) {
		log.info("Finding user '{}' in repo '{}'.", userId, repoId.id());
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
	public UserProfile getUser(RepoId repoId, UserId userId) throws InvalidUserException {
		if(!userCache.containsKey(userId)){
			throw new InvalidUserException("User '%s' does not exist".formatted(userId));
		}
		return userCache.get(userId);
	}
	
	@Override
	public boolean addPermissionToUser(RepoId repoId, Permission<UserId> permission) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO UserPermissions(user_id, path, type) VALUES(?,?,?)")){
			statement.setString(1, permission.getId());
			statement.setString(2, permission.getPath().toString());
			statement.setString(3, permission.getPermission().name());
			statement.executeUpdate();
			if(userCache.containsKey(permission.id())){
				userCache.get(permission.id()).getPermissions().put(permission.getPath().toString(), permission);
			}
			return true;
		} catch(Exception e){
			log.error("Failed to add permission to user", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean removePermissionFromUser(RepoId repoId, UserId userId, TargetPath path) {
		log.info("Removing permission '{}' from user '{}' in repo '{}'", path, userId, repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM UserPermissions WHERE user_id = ? AND path = ?")){
			statement.setString(1, userId.id());
			statement.setString(2, path.toString());
			statement.executeUpdate();
			if(userCache.containsKey(userId)){
				userCache.get(userId).getPermissions().remove(path.toString());
			}
			log.info("Permission '{}' removed from user '{}' in repo '{}'", path, userId, repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to remove permission from user", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@SuppressWarnings("DuplicatedCode") // Duplicated code is fine here the copy is for group permissions
	@Override
	public boolean updatePermissionForUser(RepoId repoId, Permission<UserId> permission) {
		log.info("Updating permission '{}' in user '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("UPDATE UserPermissions SET type = ? WHERE user_id = ? AND path = ?")){
			statement.setString(1, permission.getPermission().name());
			statement.setString(2, permission.getId());
			statement.setString(3, permission.getPath().toString());
			statement.executeUpdate();
			if(userCache.containsKey(permission.id())){
				userCache.get(permission.id()).getPermissions().get(permission.getPath().toString()).setPermission(permission.getPermission());
			}
			log.info("Permission '{}' updated in user '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to update permission for user", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean userExists(RepoId repoId, UserId userId) throws InvalidUserException, InvalidRepoException {
		if(!userCache.containsKey(userId)){
			return false;
		}
		return userCache.get(userId) != null;
	}
	
	@Override
	public boolean groupExists(RepoId repoId, GroupId groupId) {
		return groupCache.containsKey(groupId);
	}
	
	@Override
	public boolean userInGroup(RepoId repoId, GroupId groupId, UserId userId) {
		if(!groupUsers.containsKey(groupId)){
			return false;
		}
		return groupUsers.get(groupId).contains(userId);
	}
	
	@Override
	public boolean addGroup(RepoId repoId, Group group) throws CoreException {
		log.info("Creating group '{}' in repo '{}'", group.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement(
				"INSERT INTO Groups(group_id, group_name, created_by, created_at,last_modified_by, last_modified_at) VALUES(?,?,?,?,?,?)")){
			statement.setString(1, group.getId().id());
			statement.setString(2, group.getName());
			statement.setString(3, group.getCreatedBy());
			statement.setString(4, DateHelper.fromDateTime(group.getCreationDate()));
			statement.setString(5, null);
			statement.setString(6, null);
			statement.executeUpdate();
			groupCache.put(group.getId(), group);
			log.info("Group '{}' created in repo '{}'", group.getId(), repoId.id());
			return true;
		} catch(Exception e){
			String errorResponse = "Failed to create group '%s'".formatted(group.getId());
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean removeGroup(RepoId repoId, GroupId groupId) {
		log.info("Removing group '{}' from repo '{}'", groupId, database.getRepoProperties().getId());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM Groups WHERE group_id = ?")){
			statement.setString(1, groupId.id());
			boolean wasRemoved = statement.executeUpdate() > 0;
			if(wasRemoved){
				groupCache.remove(groupId);
			}
			log.info("Group '{}' removed from repo '{}'", groupId, database.getRepoProperties().getId());
			return wasRemoved;
		} catch(Exception e){
			log.error("Failed to delete group", e);
			return false;
		} finally{
			closeConnection(connection);
			
		}
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
	public Group renameGroup(RepoId repoId, GroupId groupId, String newName) throws CoreException {
		log.info("Renaming group '{}' in repo '{}' to '{}'", groupId, repoId.id(), newName);
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("UPDATE Groups SET group_name = ? WHERE group_id = ?")){
			statement.setString(1, newName);
			statement.setString(2, groupId.id());
			statement.executeUpdate();
			Group group = groupCache.get(groupId);
			group.setName(newName);
			log.info("Group '{}' in repo '{}' renamed to '{}'", groupId, repoId.id(), newName);
			return group;
		} catch(Exception e){
			log.error("Failed to rename group", e);
			throw new CoreSqlException("Failed to rename group", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean addUserToGroup(RepoId repoId, GroupId groupId, UserId userId) throws CoreException {
		log.info("Adding user '{}' to group '{}' in repo '{}'", userId, groupId, repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO UserGroups(user_id, group_id) VALUES(?,?)")){
			statement.setString(1, userId.id());
			statement.setString(2, groupId.id());
			statement.executeUpdate();
			
			groupUsers.computeIfAbsent(groupId, g -> new ArrayList<>()).add(userId);
			userGroups.computeIfAbsent(userId, u -> new ArrayList<>()).add(groupId);
			if(groupCache.containsKey(groupId)){
				groupCache.get(groupId).getUserIds().add(userId);
			}
			if(userCache.containsKey(userId)){
				userCache.get(userId).getGroups().add(groupId);
			}
			log.info("User '{}' added to group '{}' in repo '{}'", userId, groupId, repoId.id());
			return true;
		} catch(Exception e){
			String errorResponse = "Failed to add user '%s' to group '%s'".formatted(userId, groupId);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean removeUserFromGroup(RepoId repoId, GroupId groupId, UserId userId) throws CoreException {
		log.info("Removing user '{}' from group '{}' in repo '{}'", userId, groupId, repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM UserGroups WHERE user_id = ? and group_id = ?")){
			statement.setString(1, userId.id());
			statement.setString(2, groupId.id());
			statement.executeUpdate();
			
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
			log.info("User '{}' removed from group '{}' in repo '{}'", userId, groupId, repoId.id());
			
			return true;
		} catch(Exception e){
			String errorResponse = "Error in repository '%s' while removing user '%s' from group '%s'".formatted(database.getRepoProperties().getId(),
					userId,
					groupId);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean addPermissionToGroup(RepoId repoId, Permission<GroupId> permission) {
		log.info("Adding permission '{}' to group '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO GroupPermissions(group_id, path, type) VALUES(?,?,?)")){
			statement.setString(1, permission.getId());
			statement.setString(2, permission.getPath().toString());
			statement.setString(3, permission.getPermission().name());
			statement.executeUpdate();
			if(groupCache.containsKey(permission.id())){
				groupCache.get(permission.id()).getPermissions().put(permission.getPath().toString(), permission);
			}
			log.info("Permission '{}' added to group '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to add permission to group", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, TargetPath path) {
		log.info("Removing permission '{}' from group '{}' in repo '{}'", path, groupId, repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM GroupPermissions WHERE group_id = ? AND path = ?")){
			statement.setString(1, groupId.id());
			statement.setString(2, path.toString());
			statement.executeUpdate();
			if(groupCache.containsKey(groupId)){
				groupCache.get(groupId).getPermissions().remove(path.toString());
			}
			log.info("Permission '{}' removed from group '{}' in repo '{}'", path, groupId, repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to remove permission from group", e);
			return false;
			
		} finally{
			closeConnection(connection);
		}
	}
	
	@SuppressWarnings("DuplicatedCode") // Duplicated code is fine here the copy is for user permissions
	@Override
	public boolean updatePermissionForGroup(RepoId repoId, Permission<GroupId> permission) {
		log.info("Updating permission '{}' in group '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("UPDATE GroupPermissions SET type = ? WHERE group_id = ? AND path = ?")){
			statement.setString(1, permission.getPermission().name());
			statement.setString(2, permission.getId());
			statement.setString(3, permission.getPath().toString());
			statement.executeUpdate();
			if(groupCache.containsKey(permission.id())){
				groupCache.get(permission.id()).getPermissions().get(permission.getPath().toString()).setPermission(permission.getPermission());
			}
			log.info("Permission '{}' updated in group '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to update permission in group", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public List<Group> getGroupsFromUser(RepoId repoId, UserId userId) throws InvalidRepoException {
		List<GroupId> groupIds = userGroups.get(userId);
		if(groupIds == null){
			return new ArrayList<>();
		}
		return List.of(groupIds.stream().map(groupCache::get).toArray(Group[]::new));
	}
	
	@Override
	public List<UserProfile> getUsersFromGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException {
		List<UserId> userIds = groupUsers.get(groupId);
		if(userIds == null){
			return new ArrayList<>();
		}
		return List.of(userIds.stream().map(userCache::get).toArray(UserProfile[]::new));
	}
	
	/**
	 * Checks if a group has a permission set for the specified path
	 *
	 * @param groupId the group to check
	 * @param path the path to check
	 * @return true if the group has the permission
	 */
	public boolean groupHasPermission(GroupId groupId, TargetPath path) {
		if(!groupCache.containsKey(groupId)){
			return false;
		}
		return groupCache.get(groupId).getPermissions().containsKey(path.toString());
	}
	
	/**
	 * Checks if a group has a specific permission set for the specified path
	 *
	 * @param groupId the group to check
	 * @param path the path to check
	 * @param permission the permission to check
	 * @return true if the group has the permission
	 */
	public boolean groupHasPermission(GroupId groupId, TargetPath path, PermissionType permission) {
		if(!groupCache.containsKey(groupId)){
			return false;
		}
		Permission<GroupId> groupIdPermission = groupCache.get(groupId).getPermissions().get(path.toString());
		return groupIdPermission != null && groupIdPermission.getPermission().equals(permission);
	}
	
	/**
	 * Checks if a user has a permission set for the specified path
	 *
	 * @param userId the user to check
	 * @param path the path to check
	 * @return true if the user has the permission
	 */
	public boolean userHasPermission(UserId userId, TargetPath path) {
		if(!userCache.containsKey(userId)){
			return false;
		}
		return userCache.get(userId).getPermissions().containsKey(path.toString());
	}
	
	/**
	 * Checks if a user has a specific permission set for the specified path
	 *
	 * @param userId the user to check
	 * @param path the path to check
	 * @param permission the permission to check
	 * @return true if the user has the permission
	 */
	public boolean userHasPermission(UserId userId, TargetPath path, PermissionType permission) {
		if(!userCache.containsKey(userId)){
			return false;
		}
		Permission<UserId> userIdPermission = userCache.get(userId).getPermissions().get(path.toString());
		return userIdPermission != null && userIdPermission.getPermission().equals(permission);
	}
	
	private void closeConnection(Connection connection) {
		try{
			connection.close();
		} catch(SQLException e){
			log.error("Error while closing connection", e);
		}
	}
}
