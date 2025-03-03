package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserFunctions{
	private static final Logger log = LoggerFactory.getLogger(UserFunctions.class);
	
	public static int addUser(RepositoryDatabase database, UserId userId, String password, String createdBy) throws RuntimeSQLException {
		try(var statement = database.getConnection().prepareStatement(
				"INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(?,?,?,?)")){
			statement.setString(1, userId.toString());
			statement.setString(2, password);
			statement.setString(3, createdBy);
			statement.setString(4, createdBy);
			return statement.executeUpdate();
		} catch(Exception e){
			log.error("Failed to add user", e);
			throw new RuntimeSQLException("Failed to add user", e);
		}
	}
	
	public static List<UserId> getUsersFromGroup(RepositoryDatabase database, GroupId groupId) throws RuntimeSQLException {
		try(var statement = database.getConnection().prepareStatement("SELECT user_id FROM GroupUsers WHERE group_id = ?")){
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
			throw new RuntimeSQLException("Failed to get users from group", e);
			
		}
	}
	
	public static List<GroupId> getGroupsFromUser(RepositoryDatabase database, UserId userId) throws RuntimeSQLException {
		try(var statement = database.getConnection().prepareStatement("SELECT group_id FROM GroupUsers WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				List<GroupId> groups = new ArrayList<>();
				while(rs.next()){
					groups.add(new GroupId(rs.getString("group_id")));
				}
				return groups;
			}
		} catch(Exception e){
			log.error("Failed to get groups from user", e);
			throw new RuntimeSQLException("Failed to get groups from user", e);
		}
	}
	
	public static UserProfile getUser(RepositoryDatabase database, UserId userId) throws RuntimeSQLException {
		try(var statement = database.getConnection().prepareStatement("SELECT * FROM Users WHERE user_id = ?")){
			statement.setString(1, userId.toString());
			try(var rs = statement.executeQuery()){
				if(rs.next()){
					//todo:jmd get the permissions properly
					return new UserProfile(new UserId(rs.getString("user_id")),
							rs.getString("password_hash"),
							rs.getString("created_by"),
							rs.getString("last_modified_by"));
				}
				return null;
			}
		} catch(Exception e){
			log.error("Failed to get user", e);
			throw new RuntimeSQLException("Failed to get user", e);
		}
	}
	
}
