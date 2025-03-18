package com.wonkglorg.docapi;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.ResourcePath;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.PermissionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

class PermissionTest {
    @Test
    void canResolveFullPath() {
        Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/file.md", PermissionType.ADMIN));
        Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("path/file.md", PermissionType.VIEW));

        List<Path> paths = List.of(Path.of("path/file.md"));


        var map = Permission.filterPathsWithPermissions(userPermissions, groupPermissions, paths);

        Assertions.assertEquals(PermissionType.ADMIN, map.get(Path.of("path/file.md")));
    }


    @Test
    void canResolveAntPath() {
        Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/**", PermissionType.ADMIN));
        Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("path/**", PermissionType.VIEW));

        List<Path> paths = List.of(Path.of("path/file.md"));


        var map = Permission.filterPathsWithPermissions(userPermissions, groupPermissions, paths);

        Assertions.assertEquals(PermissionType.ADMIN, map.get(Path.of("path/file.md")));
    }


    @Test
    void userPermTakesPriorityOverGroup() {
        Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path\\**", PermissionType.ADMIN));
        Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("**", PermissionType.VIEW));

        List<Path> paths = List.of(Path.of("path/file.md"));


        var map = Permission.filterPathsWithPermissions(userPermissions, groupPermissions, paths);

        Assertions.assertEquals(PermissionType.ADMIN, map.get(Path.of("path\\file.md")));
    }

    private Permission<UserId> createUserPerm(String path, PermissionType type) {
        return new Permission<>(new UserId("test"), type, new ResourcePath(path), new RepoId("test"));
    }

    private Permission<GroupId> createGroupPerm(String path, PermissionType type) {
        return new Permission<>(new GroupId("test"), type, new ResourcePath(path), new RepoId("test"));
    }
}
