package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.ResourcePath;
import com.wonkglorg.doc.core.objects.RoleId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.permissions.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permission related database functions
 */
public class PermissionFunctions{
	private static final Logger log = LoggerFactory.getLogger(PermissionFunctions.class);
	
	/**
	 * Get all permissions for a user
	 *
	 * @param database the database to execute the function for
	 * @param userId the user to get permissions for
	 * @return a list of permissions for the user
	 */
	public static Set<Permission<UserId>> getPermissionsForUser(RepositoryDatabase database, UserId userId) {
		Connection connection = database.getConnection();
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM UserPermissions WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				Set<Permission<UserId>> permissions = new HashSet<>();
				while(rs.next()){
					permissions.add(new Permission<>(new UserId(rs.getString("user_id")),
							PermissionType.valueOf(rs.getString("permission")),
							new ResourcePath(rs.getString("path")),
							database.getRepoId()));
				}
				return permissions;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get permissions for user";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
		
	}
	
	public static List<Permission<GroupId>> getPermissionsForGroup(RepositoryDatabase database, GroupId groupId) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT * FROM GroupPermissions WHERE group_id = ?")){
			statement.setString(1, groupId.toString());
			try(var rs = statement.executeQuery()){
				List<Permission<GroupId>> permissions = new ArrayList<>();
				while(rs.next()){
					permissions.add(new Permission<>(new GroupId(rs.getString("group_id")),
							PermissionType.valueOf(rs.getString("permission")),
							new ResourcePath(rs.getString("path")),
							database.getRepoId()));
				}
				return permissions;
			}
			
		} catch(Exception e){
			String errorResponse = "Failed to get permissions for group";
			log.error(errorResponse, e);
			throw new CoreSqlException(errorResponse, e);
		} finally{
			closeConnection(connection);
		}
	}
	
	public static Set<Role> getRolesForUser(RepositoryDatabase database, UserId userId) {
		Connection connection = database.getConnection();
		try(var statement = connection.prepareStatement("SELECT * FROM UserRoles WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				Set<Role> roles = new HashSet<>();
				while(rs.next()){
					roles.add(new Role(RoleId.of(rs.getString("role_id")), rs.getString("role")));
				}
				return roles;
			}
		} catch(Exception e){
			String errorResponse = "Failed to get roles for user";
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
}
