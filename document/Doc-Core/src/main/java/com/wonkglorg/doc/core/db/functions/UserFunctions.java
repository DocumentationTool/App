package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserFunctions{
	private static final Logger log = LoggerFactory.getLogger(UserFunctions.class);
	
	/**
	 * Adds a new User to the Database
	 *
	 * @param database the database to add the user to
	 * @param userId the users id
	 * @param password their hashed password
	 * @param createdBy the admin or system who created this user
	 */
	public static boolean addUser(RepositoryDatabase database, UserId userId, String password, String createdBy) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(?,?,?,?)")){
			statement.setString(1, userId.toString());
			statement.setString(2, password);
			statement.setString(3, createdBy);
			statement.setString(4, createdBy);
			return true;
		} catch(Exception e){
			String errorResponse = "Failed to add user";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Adds a user to a specific group
	 *
	 * @param database the database to add the user to
	 * @param userId the user to add
	 * @param groupId the group to add them to
	 */
	public static void addUserToGroup(RepositoryDatabase database, UserId userId, GroupId groupId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("INSERT INTO UserGroups(user_id, group_id) VALUES(?,?)")){
			statement.setString(1, userId.id());
			statement.setString(2, groupId.id());
			statement.executeUpdate();
		} catch(Exception e){
			String errorResponse = "Failed to add user '%s' to group '%s'".formatted(userId, groupId);
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public static void removeUserFromGroup(RepositoryDatabase database, UserId userId, GroupId groupId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM UserGroups WHERE user_id = ? and group_id = ?")){
			statement.setString(1, userId.id());
			statement.setString(2, groupId.id());
			statement.executeUpdate();
		} catch(Exception e){
			String errorReponse = "Error in repository '%s' while removing user '%s' from group '%s'".formatted(database.getRepoProperties().getId(),
					userId,
					groupId);
			log.error(errorReponse, e);
			throw new CoreSqlException(errorReponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	/**
	 * Gets the ids of all users contained within a specific group
	 *
	 * @param database the database to add the user from / to
	 * @param groupId the groups id
	 */
	public static List<UserId> getUsersFromGroup(RepositoryDatabase database, GroupId groupId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT user_id FROM UserGroups WHERE group_id = ?")){
			statement.setString(1, groupId.toString());
			try(var rs = statement.executeQuery()){
				List<UserId> users = new ArrayList<>();
				while(rs.next()){
					users.add(new UserId(rs.getString("user_id")));
				}
				return users;
			}
		} catch(Exception e){
			log.error("Failed to get users from group", e);
			throw new CoreSqlException("Failed to get users from group", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public static List<GroupId> getGroupsFromUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT group_id FROM UserGroups WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				List<GroupId> groups = new ArrayList<>();
				while(rs.next()){
					groups.add(new GroupId(rs.getString("group_id")));
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
	
	public static void getAllUserGroups(RepositoryDatabase database, Map<UserId, List<GroupId>> userGroups, Map<GroupId, List<UserId>> userIds) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT * FROM UserGroups")){
			try(var rs = statement.executeQuery()){
				while(rs.next()){
					UserId userId = new UserId(rs.getString("user_id"));
					GroupId groupId = new GroupId(rs.getString("group_id"));
					userGroups.computeIfAbsent(userId, k -> new ArrayList<>()).add(groupId);
					userIds.computeIfAbsent(groupId, k -> new ArrayList<>()).add(userId);
				}
			}
		} catch(Exception e){
			log.error("Failed to get all user groups", e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public static List<Group> getAllGroups(RepositoryDatabase database) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT * FROM Groups")){
			try(var rs = statement.executeQuery()){
				List<Group> groups = new ArrayList<>();
				while(rs.next()){
					groups.add(new Group(new GroupId(rs.getString("group_id")),
							rs.getString("group_name"),
							rs.getString("created_by"),
							rs.getString("creation_date"),
							rs.getString("modified_by"),
							rs.getString("last_modified_at")
					
					));
					
				}
				return groups;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get all groups";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public static List<UserProfile> getAllUsers(RepositoryDatabase database) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT * FROM Users")){
			try(var rs = statement.executeQuery()){
				List<UserProfile> users = new ArrayList<>();
				while(rs.next()){
					String userId = rs.getString("user_id");
					String passwordHash = rs.getString("password_hash");
					var userPermissions = PermissionFunctions.getPermissionsForUser(database, new UserId(userId));
					var userRoles = PermissionFunctions.getRolesForUser(database, new UserId(userId));
					users.add(new UserProfile(new UserId(userId), passwordHash, userPermissions, userRoles));
				}
				return users;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get all users";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		}
	}
	
	/**
	 * Gets a user from the database or null if no user was found
	 *
	 * @param database the database to get the user from
	 * @param userId the id of the user to get
	 */
	public static UserProfile getUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT * FROM Users WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				if(rs.next()){
					String passwordHash = rs.getString("password_hash");
					var userPermissions = PermissionFunctions.getPermissionsForUser(database, userId);
					var userRoles = PermissionFunctions.getRolesForUser(database, userId);
					return new UserProfile(userId, passwordHash, userPermissions, userRoles);
				}
				return null;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get user";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
		
	}
	
	private static void closeConnection(Connection connection) {
		try{
			connection.close();
		} catch(SQLException e){
			log.error("Error while closing connection", e);
		}
	}
	
	public static boolean deleteUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("DELETE FROM Users WHERE user_id = ?")){
			statement.setString(1, userId.id());
			return statement.executeUpdate() > 0;
		} catch(Exception e){
			String errorResponse = "Failed to delete user '%s' in '%s'".formatted(userId, database.getRepoId());
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public static boolean createGroup(RepositoryDatabase database, GroupId groupId) {
		throw new RuntimeException("Group creation not implemented yet");
	}
	
	public static void deleteGroup(RepositoryDatabase database, GroupId groupId) {
		throw new RuntimeException("Group deletion not implemented yet");
	}
}
