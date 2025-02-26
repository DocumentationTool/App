package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.permissions.PermissionType;

import java.nio.file.Path;
import java.util.List;

public interface PermissionFunctions {


    List<PermissionType> getPermissionsForUser();

    PermissionType getPermissionForFolder(String userId, Path path);

    List<PermissionType> getAllPermissions();

}
