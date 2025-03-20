package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.objects.DateHelper;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class UserFunctions {
    private static final Logger log = LoggerFactory.getLogger(UserFunctions.class);

    /**
     * Adds a new User to the Database
     *
     * @param database  the database to add the user to
     * @param userId    the users id
     * @param password  their hashed password
     * @param createdBy the admin or system who created this user
     */
    public static boolean addUser(RepositoryDatabase database, UserId userId, String password, String createdBy) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(?,?,?,?)")) {
            statement.setString(1, userId.id());
            statement.setString(2, password);
            statement.setString(3, createdBy);
            statement.setString(4, createdBy);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            String errorResponse = "Failed to add user";
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Adds a user to a specific group
     *
     * @param database the database to add the user to
     * @param userId   the user to add
     * @param groupId  the group to add them to
     */
    public static boolean addUserToGroup(RepositoryDatabase database, UserId userId, GroupId groupId) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("INSERT INTO UserGroups(user_id, group_id) VALUES(?,?)")) {
            statement.setString(1, userId.id());
            statement.setString(2, groupId.id());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            String errorResponse = "Failed to add user '%s' to group '%s'".formatted(userId, groupId);
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    public static boolean removeUserFromGroup(RepositoryDatabase database, UserId userId, GroupId groupId) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM UserGroups WHERE user_id = ? and group_id = ?")) {
            statement.setString(1, userId.id());
            statement.setString(2, groupId.id());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            String errorResponse = "Error in repository '%s' while removing user '%s' from group '%s'".formatted(database.getRepoProperties().getId(),
                    userId,
                    groupId);
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Gets the ids of all users contained within a specific group
     *
     * @param database the database to add the user from / to
     * @param groupId  the groups id
     */
    public static List<UserId> getUsersFromGroup(RepositoryDatabase database, GroupId groupId) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("SELECT user_id FROM UserGroups WHERE group_id = ?")) {
            statement.setString(1, groupId.toString());
            try (var rs = statement.executeQuery()) {
                List<UserId> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(UserId.of(rs.getString("user_id")));
                }
                return users;
            }
        } catch (Exception e) {
            log.error("Failed to get users from group", e);
            throw new CoreSqlException("Failed to get users from group", e);
        } finally {
            closeConnection(connection);
        }
    }

    public static Set<GroupId> getGroupsFromUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("SELECT group_id FROM UserGroups WHERE user_id = ?")) {
            statement.setString(1, userId.toString());
            try (var rs = statement.executeQuery()) {
                Set<GroupId> groups = new HashSet<>();
                while (rs.next()) {
                    groups.add(GroupId.of(rs.getString("group_id")));
                }
                return groups;
            }
        } catch (Exception e) {
            String errorResponse = "Failed to get groups from user";
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    public static void getAllUserGroups(RepositoryDatabase database, Map<UserId, List<GroupId>> userGroups, Map<GroupId, List<UserId>> userIds) {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("SELECT * FROM UserGroups")) {
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    UserId userId = UserId.of(rs.getString("user_id"));
                    GroupId groupId = GroupId.of(rs.getString("group_id"));
                    userGroups.computeIfAbsent(userId, k -> new ArrayList<>()).add(groupId);
                    userIds.computeIfAbsent(groupId, k -> new ArrayList<>()).add(userId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get all user groups", e);
        } finally {
            closeConnection(connection);
        }
    }

    public static List<Group> getAllGroups(RepositoryDatabase database) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("SELECT * FROM Groups")) {
            try (var rs = statement.executeQuery()) {
                List<Group> groups = new ArrayList<>();
                while (rs.next()) {
                    groups.add(new Group(GroupId.of(rs.getString("group_id")),
                            rs.getString("group_name"),
                            rs.getString("created_by"),
                            rs.getString("created_at"),
                            rs.getString("last_modified_by"),
                            rs.getString("last_modified_at")

                    ));

                }
                return groups;
            }
        } catch (Exception e) {
            String errorResponse = "Failed to get all groups";
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    public static List<UserProfile> getAllUsers(RepositoryDatabase database) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("SELECT * FROM Users")) {
            try (var rs = statement.executeQuery()) {
                List<UserProfile> users = new ArrayList<>();
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    String passwordHash = rs.getString("password_hash");
                    var userPermissions = PermissionFunctions.getPermissionsForUser(database, UserId.of(userId));
                    var userRoles = PermissionFunctions.getRolesForUser(database, UserId.of(userId));
                    var groups = getGroupsFromUser(database, UserId.of(userId));
                    users.add(new UserProfile(UserId.of(userId), passwordHash, userPermissions, userRoles, groups));
                }
                return users;
            }
        } catch (Exception e) {
            String errorResponse = "Failed to get all users";
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        }
    }

    /**
     * Gets a user from the database or null if no user was found
     *
     * @param database the database to get the user from
     * @param userId   the id of the user to get
     */
    public static UserProfile getUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("SELECT * FROM Users WHERE user_id = ?")) {
            statement.setString(1, userId.toString());
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    var userPermissions = PermissionFunctions.getPermissionsForUser(database, userId);
                    var userRoles = PermissionFunctions.getRolesForUser(database, userId);
                    var groups = getGroupsFromUser(database, userId);
                    return new UserProfile(userId, passwordHash, userPermissions, userRoles, groups);
                }
                return null;
            }
        } catch (Exception e) {
            String errorResponse = "Failed to get user";
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }

    }

    private static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Error while closing connection", e);
        }
    }

    public static boolean deleteUser(RepositoryDatabase database, UserId userId) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM Users WHERE user_id = ?")) {
            statement.setString(1, userId.id());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            String errorResponse = "Failed to delete user '%s' in '%s'".formatted(userId, database.getRepoId());
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Creates a new group
     *
     * @param database the database to create the group in
     * @param group    the group to create
     * @return true if the group was created successfully
     * @throws CoreSqlException if the group could not be created
     */
    public static boolean createGroup(RepositoryDatabase database, Group group) throws CoreSqlException {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement(
                "INSERT INTO Groups(group_id, group_name, created_by, created_at,last_modified_by, last_modified_at) VALUES(?,?,?,?,?,?)")) {
            statement.setString(1, group.getId().id());
            statement.setString(2, group.getName());
            statement.setString(3, group.getCreatedBy());
            statement.setString(4, DateHelper.fromDateTime(group.getCreationDate()));
            statement.setString(5, null);
            statement.setString(6, null);
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            String errorResponse = "Failed to create group '%s'".formatted(group.getId());
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    public static boolean deleteGroup(RepositoryDatabase database, GroupId groupId) {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM Groups WHERE group_id = ?")) {
            statement.setString(1, groupId.id());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Failed to delete group", e);
        } finally {
            closeConnection(connection);

        }


        throw new RuntimeException("Group deletion not implemented yet");
    }

    /**
     * Renames a group
     *
     * @param database
     * @param groupId
     * @param newName
     */
    public static void renameGroup(RepositoryDatabase database, GroupId groupId, String newName) {
        Connection connection = database.getConnection();
        try (var statement = connection.prepareStatement("UPDATE Groups SET group_name = ? WHERE group_id = ?")) {
            statement.setString(1, newName);
            statement.setString(2, groupId.id());
            statement.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to rename group", e);
        } finally {
            closeConnection(connection);

        }
    }
}
