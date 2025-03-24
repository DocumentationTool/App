package com.wonkglorg.doc.core.user;

import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.permissions.Permission;

import java.util.HashMap;
import java.util.Map;

public class RepositoryGroup extends Group {

    private final Map<String, Permission<GroupId>> permissions = new HashMap<>();
}
