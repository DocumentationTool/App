package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.UserDatabase;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.interfaces.PermissionCalls;
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Permission related database functions
 */
public class PermissionFunctions implements IDBFunctions, PermissionCalls{
	private static final Logger log = LoggerFactory.getLogger(PermissionFunctions.class);
	private final Map<UserId, Map<String, Permission<UserId>>> userPermissions = new HashMap<>();
	private final Map<GroupId, Map<String, Permission<GroupId>>> groupPermissions = new HashMap<>();
	private final RepositoryDatabase database;
	
	public PermissionFunctions(RepositoryDatabase database) {
		this.database = database;
	}
	
	@Override
	public void initialize() {
		log.info("Initializing cache for user functions in repo '{}'", database.getRepoProperties().getId());
		Connection connection = database.getConnection();
		try{
			UserDatabase userDB = database.getFileRepository().getUserDB();
			for(UserProfile userProfile : userDB.getUsers()){
				Set<Permission<UserId>> permissions = loadUserPermissions(connection, userProfile.getId());
				for(Permission<UserId> permission : permissions){
					userPermissions.computeIfAbsent(userProfile.getId(), k -> new HashMap<>()).put(permission.getPath().toString(), permission);
				}
			}
			
			for(Group group : userDB.getGroups()){
				Set<Permission<GroupId>> permissions = getPermissionsForGroup(connection, group.getId());
				for(Permission<GroupId> permission : permissions){
					groupPermissions.computeIfAbsent(group.getId(), k -> new HashMap<>()).put(permission.getPath().toString(), permission);
				}
			}
		} catch(CoreSqlException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Loads the permissions for a user from the database
	 *
	 * @param userId the user to get permissions for
	 * @return a list of permissions for the user
	 */
	public Set<Permission<UserId>> loadUserPermissions(Connection connection, UserId userId) throws CoreSqlException {
		try(PreparedStatement statement = connection.prepareStatement("SELECT type,path,user_id FROM UserPermissions WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				Set<Permission<UserId>> permissions = new HashSet<>();
				while(rs.next()){
					permissions.add(new Permission<>(UserId.of(rs.getString("user_id")),
							PermissionType.valueOf(rs.getString("type")),
							new TargetPath(rs.getString("path")),
							database.getRepoId()));
				}
				return permissions;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get permissions for user";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
		
	}
	
	public Set<Permission<GroupId>> getPermissionsForGroup(Connection connection, GroupId groupId) throws CoreSqlException {
		try(var statement = connection.prepareStatement("SELECT group_id,type,path FROM GroupPermissions WHERE group_id = ?")){
			statement.setString(1, groupId.toString());
			try(var rs = statement.executeQuery()){
				Set<Permission<GroupId>> permissions = new HashSet<>();
				while(rs.next()){
					permissions.add(new Permission<>(GroupId.of(rs.getString("group_id")),
							PermissionType.valueOf(rs.getString("type")),
							new TargetPath(rs.getString("path")),
							database.getRepoId()));
				}
				return permissions;
			}
			
		} catch(Exception e){
			String errorResponse = "Failed to get permissions for group";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	private static void closeConnection(Connection connection) {
		try{
			connection.close();
		} catch(SQLException e){
			log.error("Error while closing connection", e);
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
			groupPermissions.computeIfAbsent(permission.id(), k -> new HashMap<>()).put(permission.getPath().toString(), permission);
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
			groupPermissions.computeIfAbsent(groupId, k -> new HashMap<>()).remove(path.toString());
			log.info("Permission '{}' removed from group '{}' in repo '{}'", path, groupId, repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to remove permission from group", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@Override
	public boolean updatePermissionForGroup(RepoId repoId, Permission<GroupId> permission) {
		log.info("Updating permission '{}' in group '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("UPDATE GroupPermissions SET type = ? WHERE group_id = ? AND path = ?")){
			statement.setString(1, permission.getPermission().name());
			statement.setString(2, permission.getId());
			statement.setString(3, permission.getPath().toString());
			statement.executeUpdate();
			groupPermissions.computeIfAbsent(permission.id(), k -> new HashMap<>()).put(permission.getPath().toString(), permission);
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
	public boolean addPermissionToUser(RepoId repoId, Permission<UserId> permission) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO UserPermissions(user_id, path, type) VALUES(?,?,?)")){
			statement.setString(1, permission.getId());
			statement.setString(2, permission.getPath().toString());
			statement.setString(3, permission.getPermission().name());
			statement.executeUpdate();
			
			userPermissions.computeIfAbsent(permission.id(), k -> new HashMap<>()).put(permission.getPath().toString(), permission);
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
			
			userPermissions.computeIfAbsent(userId, k -> new HashMap<>()).remove(path.toString());
			log.info("Permission '{}' removed from user '{}' in repo '{}'", path, userId, repoId.id());
			return true;
		} catch(Exception e){
			log.error("Failed to remove permission from user", e);
			return false;
		} finally{
			closeConnection(connection);
		}
	}
	
	@SuppressWarnings("DuplicatedCode") // This is a duplicate of the same method in GroupFunctions
	@Override
	public boolean updatePermissionForUser(RepoId repoId, Permission<UserId> permission) {
		log.info("Updating permission '{}' in user '{}' in repo '{}'", permission.getPath(), permission.getId(), repoId.id());
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("UPDATE UserPermissions SET type = ? WHERE user_id = ? AND path = ?")){
			statement.setString(1, permission.getPermission().name());
			statement.setString(2, permission.getId());
			statement.setString(3, permission.getPath().toString());
			statement.executeUpdate();
			
			userPermissions.computeIfAbsent(permission.id(), k -> new HashMap<>()).put(permission.getPath().toString(), permission);
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
	public Set<Permission<UserId>> getPermissionsForUser(RepoId repoId, UserId userId) {
		if(!userPermissions.containsKey(userId)){
			return Set.of();
		}
		
		var permissionMap = userPermissions.get(userId);
		if(permissionMap == null){
			return Set.of();
		}
		
		return new HashSet<>(permissionMap.values());
	}
	
	@Override
	public Set<Permission<GroupId>> getPermissionsForGroup(RepoId repoId, GroupId groupId) {
		if(!groupPermissions.containsKey(groupId)){
			return Set.of();
		}
		
		var permissionMap = groupPermissions.get(groupId);
		if(permissionMap == null){
			return Set.of();
		}
		
		return new HashSet<>(permissionMap.values());
	}
	
	/**
	 * Checks if a group has a permission set for the specified path
	 *
	 * @param groupId the group to check
	 * @param path the path to check
	 * @return true if the group has the permission
	 */
	public boolean groupHasPermission(GroupId groupId, TargetPath path) {
		
		if(!groupPermissions.containsKey(groupId)){
			return false;
		}
		return groupPermissions.get(groupId).containsKey(path.toString());
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
		if(!groupPermissions.containsKey(groupId)){
			return false;
		}
		
		Permission<GroupId> groupIdPermission = groupPermissions.get(groupId).get(path.toString());
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
		if(!userPermissions.containsKey(userId)){
			return false;
		}
		return userPermissions.get(userId).containsKey(path.toString());
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
		if(!userPermissions.containsKey(userId)){
			return false;
		}
		
		Permission<UserId> userIdPermission = userPermissions.get(userId).get(path.toString());
		return userIdPermission != null && userIdPermission.getPermission().equals(permission);
	}
	
	/**
	 * Cleans up the group when it is no longer available, should be called when a group is removed
	 */
	public void cleanUpGroup(GroupId groupId) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM GroupPermissions WHERE group_id = ?")){
			statement.setString(1, groupId.id());
			statement.executeUpdate();
			groupPermissions.remove(groupId);
		} catch(Exception e){
			log.error("Failed to clean up group", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public void cleanUpUser(UserId userId) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM UserPermissions WHERE user_id = ?")){
			statement.setString(1, userId.id());
			statement.executeUpdate();
			userPermissions.remove(userId);
		} catch(Exception e){
			log.error("Failed to clean up user", e);
		} finally{
			closeConnection(connection);
		}
	}
	
}
