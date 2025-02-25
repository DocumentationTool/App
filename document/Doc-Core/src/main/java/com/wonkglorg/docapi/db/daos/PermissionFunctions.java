package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.permissions.Permission;
import com.wonkglorg.docapi.user.UserProfile;

import java.nio.file.Path;
import java.util.List;

public interface PermissionFunctions {


    List<Permission> getPermissionsForUser();

    Permission getPermissionForFolder(String userId, Path path);

    List<Permission> getAllPermissions();

}
