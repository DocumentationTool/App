package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.db.exception.RuntimeSQLException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Permission related database functions
 */
public class PermissionFunctions {

    public static List<Permission<UserId>> getPermissionsForUser(RepositoryDatabase database, UserId userId) {
        try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM UserPermissions WHERE user_id = ?")) {
            statement.setString(1, userId.toString());
            try (var rs = statement.executeQuery()) {
                List<Permission<UserId>> permissions = new ArrayList<>();
                while (rs.next()) {
                    permissions.add(new Permission<>(new UserId(rs.getString("user_id")),
                            Path.of(rs.getString("path")),
                            PermissionType.valueOf(rs.getString("permission"))));
                }
                return permissions;
            }
        } catch (Exception e) {
            log.error("Failed to get permissions for user", e);
            throw new RuntimeSQLException("Failed to get permissions for user", e);

        }

    }

    public static List<Permission<GroupId>> getPermissionsForGroup(GroupId groupId) {

    }

    public static PermissionType getPermissionForFolder(UserId userId, Path path) {

    }

    public static List<PermissionType> getAllPermissions() {

    }

}
