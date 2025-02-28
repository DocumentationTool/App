package com.wonkglorg.doc.core.db.daos;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.SQLException;
import java.util.List;

public interface UserFunctions{
	
	@SqlUpdate("""
			INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(:userId,:password,:created_by,:created_by);
			""")
	int addUser(@Bind("userId") UserId userId, @Bind("password") String password, @Bind("created_by") String createdBy) throws Exception;
	
	@SqlQuery("""
			SELECT user_id FROM GroupUsers WHERE group_id = :groupId
			""")
	List<UserId> getUsersFromGroup(@Bind("groupId") GroupId groupId) throws Exception;
	
	@SqlQuery("""
			SELECT group_id FROM GroupUsers WHERE user_id = :userId
			""")
	List<GroupId> getGroupsFromUser(UserId userId) throws Exception;

	@SqlQuery("""
		SELECT * FROM Users WHERE user_id = :userId
		""")
	UserProfile getUser(@Bind("userId") UserId userId) throws Exception;



	
}
