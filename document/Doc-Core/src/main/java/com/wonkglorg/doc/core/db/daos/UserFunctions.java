package com.wonkglorg.doc.core.db.daos;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface UserFunctions{
	
	@SqlUpdate("""
			INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(:userId,:password,:created_by,:created_by);
			""")
	int addUserFunction(@Bind("userId") UserId userId, @Bind("password") String password, @Bind("created_by") String createdBy);
	
	@SqlQuery("""
			SELECT user_id FROM GroupUsers WHERE group_id = :groupId
			""")
	List<UserId> getUsersFromGroup(@Bind("groupId") GroupId groupId);
	
	@SqlQuery("""
			SELECT group_id FROM GroupUsers WHERE user_id = :userId
			""")
	List<GroupId> getGroupsFromUser(UserId userId);
	
}
