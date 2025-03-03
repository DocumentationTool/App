package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import com.wonkglorg.doc.core.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserFunctions {
    private static final Logger log = LoggerFactory.getLogger(UserFunctions.class);

    /**
     * Adds a new User to the Database
     *
     * @param database  the database to add the user to
     * @param userId    the users id
     * @param password  their hashed password
     * @param createdBy the admin or system who created this user
     * @return {@link UpdateDatabaseResponse}
     */
    public static UpdateDatabaseResponse addUser(RepositoryDatabase database, UserId userId, String password, String createdBy) {
        try (var statement = database.getConnection().prepareStatement(
                "INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(?,?,?,?)")) {
            statement.setString(1, userId.toString());
            statement.setString(2, password);
            statement.setString(3, createdBy);
            statement.setString(4, createdBy);
            return UpdateDatabaseResponse.success(statement.executeUpdate());
        } catch (Exception e) {
            String errorResponse = "Failed to add user";
            log.error(errorResponse, e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }


    /**
     * Adds a user to a specific group
     *
     * @param database the database to add the user to
     * @param userId   the user to add
     * @param groupId  the group to add them to
     * @return {@link UpdateDatabaseResponse}
     */
    public static UpdateDatabaseResponse addUserToGroup(RepositoryDatabase database, UserId userId, GroupId groupId) {
        try (var statement = database.getConnection().prepareStatement("INSERT INTO GroupUsers(user_id, group_id) VALUES(?,?)")) {
            statement.setString(1, userId.id());
            statement.setString(2, groupId.id());
            return UpdateDatabaseResponse.success(statement.executeUpdate());
        } catch (Exception e) {
            String errorResponse = "Failed to add user '%s' to group '%s'".formatted(userId, groupId);
            log.error(errorResponse, e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException(errorResponse, e));
        }
    }


    public static UpdateDatabaseResponse removeUserFromGroup(RepositoryDatabase database, UserId userId, GroupId groupId) {
        try (var statement = database.getConnection().prepareStatement("DELETE FROM GroupUsers WHERE user_id = ? and group_id = ?")) {

        } catch (Exception e) {
            String errorReponse = "Error in repository '%s' while removing user '%s' from group '%s'".formatted(database.getRepoProperties().getId(), userId, groupId);
            log.error(errorReponse, e);
            return UpdateDatabaseResponse.fail(new RuntimeSQLException(errorReponse, e));
        }
    }

    /**
     * Gets the ids of all users contained within a specific group
     *
     * @param database the database to add the user from / to
     * @param groupId  the groups id
     * @return {@link QueryDatabaseResponse}
     */
    public static QueryDatabaseResponse<List<UserId>> getUsersFromGroup(RepositoryDatabase database, GroupId groupId) {
        try (var statement = database.getConnection().prepareStatement("SELECT user_id FROM GroupUsers WHERE group_id = ?")) {
            statement.setString(1, groupId.toString());
            try (var rs = statement.executeQuery()) {
                List<UserId> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new UserId(rs.getString("user_id")));
                }
                return QueryDatabaseResponse.success(users);
            }
        } catch (Exception e) {
            log.error("Failed to get users from group", e);
            throw new RuntimeSQLException("Failed to get users from group", e);

        }
    }

    public static List<GroupId> getGroupsFromUser(RepositoryDatabase database, UserId userId) {
        try (var statement = database.getConnection().prepareStatement("SELECT group_id FROM GroupUsers WHERE user_id = ?")) {
            statement.setString(1, userId.toString());
            try (var rs = statement.executeQuery()) {
                List<GroupId> groups = new ArrayList<>();
                while (rs.next()) {
                    groups.add(new GroupId(rs.getString("group_id")));
                }
                return groups;
            }
        } catch (Exception e) {
            log.error("Failed to get groups from user", e);
            throw new RuntimeSQLException("Failed to get groups from user", e);
        }
    }

    public static UserProfile getUser(RepositoryDatabase database, UserId userId) {
        try (var statement = database.getConnection().prepareStatement("SELECT * FROM Users WHERE user_id = ?")) {
            statement.setString(1, userId.toString());
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    //todo:jmd get the permissions properly
                    return new UserProfile(new UserId(rs.getString("user_id")),
                            rs.getString("password_hash"),
                            rs.getString("created_by"),
                            rs.getString("last_modified_by"));
                }
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to get user", e);
            throw new RuntimeSQLException("Failed to get user", e);
        }
    }


}
