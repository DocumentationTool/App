package com.wonkglorg.docapi;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.path.TargetPath;
import com.wonkglorg.doc.core.permissions.Permission;
import static com.wonkglorg.doc.core.permissions.Permission.filterPathsWithPermissions;
import com.wonkglorg.doc.core.permissions.PermissionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

class PermissionTest{
	@Test
	void canResolveFullPath() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/file.md", PermissionType.ADMIN));
		Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("path/file.md", PermissionType.VIEW));
		
		List<Path> paths = List.of(Path.of("path/file.md"));
		
		var map = filterPathsWithPermissions(userPermissions, groupPermissions, paths);
		
		assertEquals(PermissionType.ADMIN, map.get(Path.of("path/file.md")));
	}
	
	@Test
	void testExactFileMatch() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/file.md", PermissionType.VIEW));
		Set<Permission<GroupId>> groupPermissions = Set.of();
		List<Path> paths = List.of(Path.of("path/file.md"));
		
		assertEquals(PermissionType.VIEW, filterPathsWithPermissions(userPermissions, groupPermissions, paths).get(Path.of("path/file.md")));
	}
	
	@Test
	void testWildcardMatch() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/*.md", PermissionType.EDIT));
		Set<Permission<GroupId>> groupPermissions = Set.of();
		List<Path> paths = List.of(Path.of("path/file.md"));
		
		assertEquals(PermissionType.EDIT, filterPathsWithPermissions(userPermissions, groupPermissions, paths).get(Path.of("path/file.md")));
	}
	
	@Test
	void testAntPatternMatch() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/**", PermissionType.ADMIN));
		Set<Permission<GroupId>> groupPermissions = Set.of();
		List<Path> paths = List.of(Path.of("path/subdir/file.md"));
		
		assertEquals(PermissionType.ADMIN, filterPathsWithPermissions(userPermissions, groupPermissions, paths).get(Path.of("path/subdir/file.md")));
	}
	
	@Test
	void testNoMatchingPermissions() {
		Set<Permission<UserId>> userPermissions = Set.of();
		Set<Permission<GroupId>> groupPermissions = Set.of();
		List<Path> paths = List.of(Path.of("path/file.md"));
		
		assertEquals(PermissionType.DENY, filterPathsWithPermissions(userPermissions, groupPermissions, paths).get(Path.of("path/file.md")));
	}
	
	@Test
	void canResolveAntPath() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/**", PermissionType.ADMIN),
				createUserPerm("path/file.md", PermissionType.VIEW),
				createUserPerm("path/*.md", PermissionType.EDIT));
		Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("path/**", PermissionType.VIEW),
				createGroupPerm("path/*.md", PermissionType.EDIT),
				createGroupPerm("path/test/file.md", PermissionType.ADMIN));
		
		List<Path> path = List.of(Path.of("path/file.md"));
		
		assertEquals(PermissionType.VIEW, filterPathsWithPermissions(userPermissions, groupPermissions, path).get(Path.of("path/file.md")));
		
		List<Path> paths = List.of(Path.of("path/test/file.md"), Path.of("path/file.md"));
		
		Map<Path, PermissionType> pathPermissionTypeMap = filterPathsWithPermissions(userPermissions, groupPermissions, paths);
		System.out.println(pathPermissionTypeMap);
		
	}
	
	@Test
	void canHandleMultiplePaths() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path/**", PermissionType.ADMIN));
		Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("path/**", PermissionType.VIEW));
		
		List<Path> paths = List.of(Path.of("path/file.md"));
		
		var map = filterPathsWithPermissions(userPermissions, groupPermissions, paths);
		
		assertEquals(PermissionType.ADMIN, map.get(Path.of("path/file.md")));
	}
	
	@Test
	void userPermTakesPriorityOverGroup() {
		Set<Permission<UserId>> userPermissions = Set.of(createUserPerm("path\\**", PermissionType.ADMIN));
		Set<Permission<GroupId>> groupPermissions = Set.of(createGroupPerm("**", PermissionType.VIEW));
		
		List<Path> paths = List.of(Path.of("path/file.md"));
		
		var map = filterPathsWithPermissions(userPermissions, groupPermissions, paths);
		
		assertEquals(PermissionType.ADMIN, map.get(Path.of("path\\file.md")));
	}
	
	private Permission<UserId> createUserPerm(String path, PermissionType type) {
		return new Permission<>(UserId.of("test"), type, new TargetPath(path), RepoId.of("test"));
	}
	
	private Permission<GroupId> createGroupPerm(String path, PermissionType type) {
		return new Permission<>(GroupId.of("test"), type, new TargetPath(path), RepoId.of("test"));
	}
}
