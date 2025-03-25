package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.hash.BCryptUtils;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.interfaces.UserCalls;
import com.wonkglorg.doc.core.objects.DateHelper;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static com.wonkglorg.doc.core.hash.BCryptUtils.hashPassword;

/**
 * Database containing the users information
 */
public class UserDatabase extends SqliteDatabase<HikariDataSource> implements UserCalls, GroupCalls {
    private static final Logger log = LoggerFactory.getLogger(UserDatabase.class);
    /**
     * The cache of user profiles for this database
     */
    private final Map<UserId, UserProfile> userCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Helper map to quickly access connections between groups and users
     */
    private final Map<GroupId, Set<UserId>> groupUsers = new java.util.concurrent.ConcurrentHashMap<>();
    /**
     * Helper map to quickly access connections between users and groups
     */
    private final Map<UserId, Set<GroupId>> userGroups = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * The cache of groups for this database
     */
    private final Map<GroupId, Group> groupCache = new java.util.concurrent.ConcurrentHashMap<>();

    public UserDatabase(Path path) {
        super(getDataSource(path));

        Connection connection = getConnection();

        try (var statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS Users (
                        user_id TEXT PRIMARY KEY NOT NULL,
                        password_hash TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS Groups (
                        group_id TEXT PRIMARY KEY NOT NULL,
                        group_name TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        created_by TEXT
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS UserGroups (
                        user_id TEXT NOT NULL,
                        group_id TEXT NOT NULL,
                        PRIMARY KEY (user_id, group_id),
                        FOREIGN KEY (user_id) REFERENCES Users(user_id),
                        FOREIGN KEY (group_id) REFERENCES Groups(group_id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS UserRoles (
                        role_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        PRIMARY KEY (role_id, user_id)
                    )
                    """);


            statement.execute("""
                    CREATE TABLE IF NOT EXISTS AuditLog (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id TEXT,
                        action TEXT NOT NULL,
                        message TEXT NULL,
                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        affected_userID TEXT NULL ,
                        affected_groupID TEXT NULL ,
                        FOREIGN KEY (user_id) REFERENCES Users(user_id),
                        FOREIGN KEY (affected_userID) REFERENCES Users(user_id),
                        FOREIGN KEY (affected_groupID) REFERENCES Groups(group_id)
                    )
                    """);


            statement.execute("""
                    INSERT OR IGNORE INTO  Users (user_id, password_hash,created_by) VALUES ('admin', '%s','system');
                    """.formatted(BCryptUtils.hashPassword("admin1")));
            statement.execute("""
                    INSERT OR IGNORE INTO Groups (group_id, group_name, created_by) VALUES ('admin', 'admin','system');
                    """);
            statement.execute("""
                        INSERT OR IGNORE INTO UserGroups(user_id, group_id) VALUES ('admin', 'admin');
                    """);

            statement.execute("""
                    INSERT OR IGNORE INTO UserRoles(role_id, user_id) VALUES ('%s', 'admin');
                    """.formatted(Role.ADMIN.name()));

            statement.execute("""
                    CREATE TRIGGER IF NOT EXISTS delete_user_cleanup
                    AFTER DELETE ON Users
                    FOR EACH ROW
                    BEGIN
                       -- Delete related permissions
                        DELETE FROM UserGroups WHERE user_id = OLD.user_id;
                        DELETE FROM UserRoles WHERE user_id = OLD.user_id;
                    END;
                    """);


            statement.execute("""
                    CREATE TRIGGER IF NOT EXISTS delete_group_cleanup
                    AFTER DELETE ON Groups
                    FOR EACH ROW
                    BEGIN
                        DELETE FROM UserGroups WHERE group_id = OLD.group_id;
                    END;
                    """);

            loadAllUserGroups(connection, userGroups, groupUsers);
            loadAllUsers(connection).forEach(user -> userCache.put(user.getId(), user));
            loadAllGroups(connection).forEach(group -> groupCache.put(group.getId(), group));

        } catch (Exception e) {
            log.error("Failed to create tables", e);
        } finally {
            closeConnection(connection);
        }

    }

    /**
     * Retrieves the data source for the current sql connection
     *
     * @param openInPath the path to open the data source in
     * @return the created data source
     */
    private static HikariDataSource getDataSource(Path openInPath) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setLeakDetectionThreshold(1000);
        hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
        return new HikariDataSource(hikariConfig);
    }

    @Override
    public boolean groupExists(GroupId groupId) {
        return groupCache.containsKey(groupId);
    }

    @Override
    public boolean userInGroup(GroupId groupId, UserId userId) {
        if (!groupUsers.containsKey(groupId)) {
            return false;
        }
        return groupUsers.get(groupId).contains(userId);
    }

    @Override
    public boolean addGroup(Group group) throws CoreException {
        log.info("Creating group '{}'", group.getId());
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement(
                "INSERT INTO Groups(group_id, group_name, created_by, created_at) VALUES(?,?,?,?)")) {
            statement.setString(1, group.getId().id());
            statement.setString(2, group.getName());
            statement.setString(3, group.getCreatedBy());
            statement.setString(4, DateHelper.fromDateTime(group.getCreationDate()));
            statement.executeUpdate();
            groupCache.put(group.getId(), group);
            log.info("Group '{}' created", group.getId());
            return true;
        } catch (Exception e) {
            String errorResponse = "Failed to create group '%s'".formatted(group.getId());
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public boolean removeGroup(GroupId groupId) {
        log.info("Removing group '{}' ", groupId);
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM Groups WHERE group_id = ?")) {
            statement.setString(1, groupId.id());
            boolean wasRemoved = statement.executeUpdate() > 0;
            if (wasRemoved) {
                groupCache.remove(groupId);
            }
            log.info("Group '{}' removed!", groupId);
            return wasRemoved;
        } catch (Exception e) {
            log.error("Failed to delete group", e);
            return false;
        } finally {
            closeConnection(connection);

        }
    }

    @Override
    public List<Group> getGroups() {
        return new ArrayList<>(groupCache.values());
    }

    @Override
    public Group getGroup(GroupId groupId) {
        return groupCache.get(groupId);
    }

    @Override
    public Group renameGroup(GroupId groupId, String newName) throws CoreException {
        log.info("Renaming group '{}' to '{}'", groupId, newName);
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement("UPDATE Groups SET group_name = ? WHERE group_id = ?")) {
            statement.setString(1, newName);
            statement.setString(2, groupId.id());
            statement.executeUpdate();
            Group group = groupCache.get(groupId);
            group.setName(newName);
            log.info("Group '{}' renamed to '{}'", groupId, newName);
            return group;
        } catch (Exception e) {
            log.error("Failed to rename group", e);
            throw new CoreSqlException("Failed to rename group", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public boolean addUserToGroup(GroupId groupId, UserId userId) throws CoreException {
        Connection connection = getConnection();
        return addUserToGroup(connection, groupId, userId);
    }

    private boolean addUserToGroup(Connection connection, GroupId groupId, UserId userId) throws CoreException {
        log.info("Adding user '{}' to group '{}'", userId, groupId);
        try (var statement = connection.prepareStatement("INSERT INTO UserGroups(user_id, group_id) VALUES(?,?)")) {
            statement.setString(1, userId.id());
            statement.setString(2, groupId.id());
            statement.executeUpdate();

            groupUsers.computeIfAbsent(groupId, g -> new HashSet<>()).add(userId);
            userGroups.computeIfAbsent(userId, u -> new HashSet<>()).add(groupId);
            if (groupCache.containsKey(groupId)) {
                groupCache.get(groupId).getUserIds().add(userId);
            }
            if (userCache.containsKey(userId)) {
                userCache.get(userId).getGroups().add(groupId);
            }
            log.info("User '{}' added to group '{}'", userId, groupId);
            return true;
        } catch (Exception e) {
            String errorResponse = "Failed to add user '%s' to group '%s'".formatted(userId, groupId);
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        }
    }

    @Override
    public boolean removeUserFromGroup(GroupId groupId, UserId userId) throws CoreException {
        log.info("Removing user '{}' from group '{}'", userId, groupId);
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM UserGroups WHERE user_id = ? and group_id = ?")) {
            statement.setString(1, userId.id());
            statement.setString(2, groupId.id());
            statement.executeUpdate();

            //update the caches
            if (groupUsers.containsKey(groupId)) {
                groupUsers.get(groupId).remove(userId);
            }
            if (userGroups.containsKey(userId)) {
                userGroups.get(userId).remove(groupId);
            }
            if (groupCache.containsKey(groupId)) {
                groupCache.get(groupId).getUserIds().remove(userId);
            }
            if (userCache.containsKey(userId)) {
                userCache.get(userId).getGroups().remove(groupId);
            }
            log.info("User '{}' removed from group '{}'", userId, groupId);

            return true;
        } catch (Exception e) {
            String errorResponse = "Error while removing user '%s' from group '%s'".formatted(userId, groupId);
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }

    }

    @Override
    public Set<Group> getGroupsFromUser(UserId userId) {
        Set<GroupId> groupIds = userGroups.get(userId);
        if (groupIds == null) {
            return new HashSet<>();
        }
        return Set.of(groupIds.stream().map(groupCache::get).toArray(Group[]::new));
    }

    @Override
    public Set<UserProfile> getUsersFromGroup(GroupId groupId) {
        Set<UserId> userIds = groupUsers.get(groupId);
        if (userIds == null) {
            return new HashSet<>();
        }
        return Set.of(userIds.stream().map(userCache::get).toArray(UserProfile[]::new));
    }

    @Override
    public boolean addUser(UserProfile user) throws CoreSqlException {
        log.info("Adding user '{}''", user.getId());
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            try (var statement = connection.prepareStatement("INSERT INTO Users(user_id, password_hash, created_by)  VALUES(?,?,?)")) {
                statement.setString(1, user.getId().id());
                statement.setString(2, hashPassword(user.getPasswordHash()));
                statement.setString(3, "system");
                statement.executeUpdate();
                userCache.put(user.getId(), user);
                log.info("User '{}' added", user.getId());
            }

            for (GroupId groupId : user.getGroups()) {
                addUserToGroup(connection, groupId, user.getId());
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new CoreSqlException("Failed to rollback statement", e);
            }
            throw new CoreSqlException("Failed to add user", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new CoreSqlException("Failed to set auto commit", e);
            }
            closeConnection(connection);
        }
    }

    @Override
    public boolean removeUser(UserId userId) throws CoreSqlException {
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM Users WHERE user_id = ?")) {
            statement.setString(1, userId.id());
            userCache.remove(userId);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            String errorResponse = "Failed to delete user '%s'".formatted(userId);
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<UserProfile> getUsers() {
        return new ArrayList<>(userCache.values());
    }

    @Override
    public UserProfile getUser(UserId userId) throws InvalidUserException {
        if (!userCache.containsKey(userId)) {
            throw new InvalidUserException("User '%s' does not exist".formatted(userId));
        }
        return userCache.get(userId);
    }

    @Override
    public boolean userExists(UserId userId) {
        if (!userCache.containsKey(userId)) {
            return false;
        }
        return userCache.get(userId) != null;
    }

    private Set<GroupId> loadGroupsFromUser(UserId userId) throws CoreSqlException {
        Connection connection = this.getConnection();
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

    /**
     * Loads all groups from the database
     *
     * @param connection the connection to the database
     * @return a list of all groups
     * @throws CoreSqlException
     */
    private List<Group> loadAllGroups(Connection connection) throws CoreSqlException {
        try (var statement = connection.prepareStatement("SELECT group_id,group_name,created_by,created_at FROM Groups")) {
            try (var rs = statement.executeQuery()) {
                List<Group> groups = new ArrayList<>();
                while (rs.next()) {
                    groups.add(new Group(GroupId.of(rs.getString("group_id")),
                            rs.getString("group_name"),
                            rs.getString("created_by"),
                            rs.getString("created_at")
                    ));
                }
                return groups;
            }
        } catch (Exception e) {
            String errorResponse = "Failed to get all groups";
            throw new CoreSqlException(errorResponse, e);
        }
    }

    /**
     * Loads all users from the database
     *
     * @param connection the connection to the database
     * @return a list of all users
     * @throws CoreSqlException
     */
    private List<UserProfile> loadAllUsers(Connection connection) throws CoreSqlException {
        try (var statement = connection.prepareStatement("SELECT user_id, password_hash FROM Users")) {
            try (var rs = statement.executeQuery()) {
                List<UserProfile> users = new ArrayList<>();
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    String passwordHash = rs.getString("password_hash");
                    users.add(new UserProfile(UserId.of(userId),
                            passwordHash,
                            userGroups.get(UserId.of(userId)),
                            loadRolesForUser(connection, UserId.of(userId))));
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
     * Loads the roles for a user
     *
     * @param connection the connection to the database
     * @param userId     the user id
     * @return the roles for the user
     * @throws CoreSqlException if the roles could not be loaded
     */
    private Set<Role> loadRolesForUser(Connection connection, UserId userId) throws CoreSqlException {
        try (var statement = connection.prepareStatement("SELECT role_id FROM UserRoles WHERE user_id = ?")) {
            statement.setString(1, userId.toString());
            try (var rs = statement.executeQuery()) {
                Set<Role> roles = new HashSet<>();
                while (rs.next()) {
                    roles.add(Role.valueOf(rs.getString("role_id")));
                }
                return roles;
            }
        } catch (Exception e) {
            String errorResponse = "Failed to get roles for user";
            log.error(errorResponse, e);
            throw new CoreSqlException(errorResponse, e);
        }
    }

    /**
     * Loads all users from the database
     *
     * @param connection the connection to the database
     * @param userGroups the map of user groups
     * @param userIds    the map of user ids
     */
    private void loadAllUserGroups(Connection connection, Map<UserId, Set<GroupId>> userGroups, Map<GroupId, Set<UserId>> userIds) {
        try (var statement = connection.prepareStatement("SELECT user_id, group_id FROM UserGroups")) {
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    UserId userId = UserId.of(rs.getString("user_id"));
                    GroupId groupId = GroupId.of(rs.getString("group_id"));
                    userGroups.computeIfAbsent(userId, k -> new HashSet<>()).add(groupId);
                    userIds.computeIfAbsent(groupId, k -> new HashSet<>()).add(userId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get all user groups", e);
        }
    }

    @Override
    public void addRole(UserId userId, Role role) {
        log.info("Adding role '{}' to user '{}'", role, userId);
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement("INSERT INTO UserRoles(user_id, role_id) VALUES(?,?)")) {
            statement.setString(1, userId.id());
            statement.setString(2, role.name());
            statement.executeUpdate();
            userCache.get(userId).getRoles().add(role);
            log.info("Role '{}' added to user '{}'", role, userId);
        } catch (SQLException e) {
            log.error("Failed to add role to user", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void removeRole(UserId userId, Role role) {
        log.info("Removing role '{}' from user '{}'", role, userId);
        Connection connection = getConnection();
        try (var statement = connection.prepareStatement("DELETE FROM UserRoles WHERE user_id = ? AND role_id = ?")) {
            statement.setString(1, userId.id());
            statement.setString(2, role.name());
            statement.executeUpdate();

            userCache.get(userId).getRoles().remove(role);
            log.info("Role '{}' removed from user '{}'", role, userId);
        } catch (SQLException e) {
            log.error("Failed to remove role from user", e);
        } finally {
            closeConnection(connection);
        }
    }

    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Error while closing connection", e);
        }
    }
}
