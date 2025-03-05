package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.ResourcePath;
import com.wonkglorg.doc.core.objects.RoleId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

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
	public static QueryDatabaseResponse<List<Permission<UserId>>> getPermissionsForUser(RepositoryDatabase database, UserId userId) {
		try(PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM UserPermissions WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				List<Permission<UserId>> permissions = new ArrayList<>();
				while(rs.next()){
					permissions.add(new Permission<>(new UserId(rs.getString("user_id")),
							PermissionType.valueOf(rs.getString("permission")),
							new ResourcePath(rs.getString("path")),
							database.getRepoId()));
				}
				return QueryDatabaseResponse.success(database.getRepoId(), permissions);
			}
		} catch(Exception e){
			String errorResponse = "Failed to get permissions for user";
			log.error(errorResponse, e);
			return QueryDatabaseResponse.error(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
		
	}
	
	public static QueryDatabaseResponse<List<Permission<GroupId>>> getPermissionsForGroup(RepositoryDatabase database, GroupId groupId) {
		try(var statement = database.getConnection().prepareStatement("SELECT * FROM GroupPermissions WHERE group_id = ?")){
			statement.setString(1, groupId.toString());
			try(var rs = statement.executeQuery()){
				List<Permission<GroupId>> permissions = new ArrayList<>();
				while(rs.next()){
					permissions.add(new Permission<>(new GroupId(rs.getString("group_id")),
							PermissionType.valueOf(rs.getString("permission")),
							new ResourcePath(rs.getString("path")),
							database.getRepoId()));
				}
				return QueryDatabaseResponse.success(database.getRepoId(), permissions);
			}
			
		} catch(Exception e){
			String errorResponse = "Failed to get permissions for group";
			log.error(errorResponse, e);
			return QueryDatabaseResponse.error(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
	
	public static QueryDatabaseResponse<List<Role>> getRolesForUser(RepositoryDatabase database, UserId userId) {
		try(var statement = database.getConnection().prepareStatement("SELECT * FROM UserRoles WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				List<Role> roles = new ArrayList<>();
				while(rs.next()){
					roles.add(new Role(RoleId.of(rs.getString("role_id")), rs.getString("role")));
				}
				return QueryDatabaseResponse.success(database.getRepoId(), roles);
			}
		} catch(Exception e){
			String errorResponse = "Failed to get roles for user";
			log.error(errorResponse, e);
			return QueryDatabaseResponse.error(database.getRepoId(), new RuntimeSQLException(errorResponse, e));
		}
	}
}
