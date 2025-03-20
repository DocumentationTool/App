package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.user.Group;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
public class GroupService implements GroupCalls {
    private final RepoService repoService;
    private final UserService userService;

    public GroupService(@Lazy RepoService repoService, UserService userService) {
        this.repoService = repoService;
        this.userService = userService;
    }


    public boolean groupExists(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        repoService.validateRepoId(repoId);
        return repoService.getRepo(repoId).getDatabase().groupExists(groupId);
    }

    @Override
    public boolean userInGroup(RepoId repoId, GroupId groupId, UserId userId) throws InvalidRepoException {
        return repoService.getRepo(repoId).getDatabase().userInGroup(repoId, groupId, userId);
    }

    @Override
    public boolean addGroup(RepoId repoId, Group group) throws InvalidRepoException, CoreException, InvalidGroupException {
        repoService.validateRepoId(repoId);
        if (groupExists(repoId, group.getId())) {
            throw new InvalidGroupException("Group with id '%s' already exists in '%s'".formatted(group.getId(), repoId));
        }
        return repoService.getRepo(repoId).getDatabase().addGroup(repoId, group);
    }

    @Override
    public boolean removeGroup(RepoId repoId, GroupId groupId) throws InvalidRepoException, InvalidGroupException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        return repoService.getRepo(repoId).getDatabase().removeGroup(repoId, groupId);
    }

    public List<Group> getGroups(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        repoService.validateRepoId(repoId);
        return repoService.getRepo(repoId).getDatabase().getGroups(repoId, groupId);
    }

    @Override
    public Group renameGroup(RepoId repoId, GroupId groupId, String newName) throws CoreException, InvalidRepoException, InvalidGroupException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        return repoService.getRepo(repoId).getDatabase().renameGroup(repoId, groupId, newName);
    }

    @Override
    public boolean addUserToGroup(RepoId repoId, GroupId groupId, UserId userId) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        userService.validateUser(repoId, userId);

        if (repoService.getRepo(repoId).getDatabase().userInGroup(repoId, groupId, userId)) {
            throw new InvalidUserException("User with id '%s' is already in group '%s'".formatted(userId, groupId));
        }

        return repoService.getRepo(repoId).getDatabase().addUserToGroup(repoId, groupId, userId);
    }

    @Override
    public boolean removeUserFromGroup(RepoId repoId, GroupId groupId, UserId userId) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        userService.validateUser(repoId, userId);

        if (!repoService.getRepo(repoId).getDatabase().userInGroup(repoId, groupId, userId)) {
            throw new InvalidUserException("User with id '%s' is not in group '%s'".formatted(userId, groupId));
        }

        return repoService.getRepo(repoId).getDatabase().removeUserFromGroup(repoId, groupId, userId);
    }

    @Override
    public boolean addPermissionToGroup(RepoId repoId, GroupId groupId, Permission<GroupId> permission) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        return repoService.getRepo(repoId).getDatabase().addPermissionToGroup(repoId, groupId, permission);
    }

    @Override
    public boolean removePermissionFromGroup(RepoId repoId, GroupId groupId, Permission<GroupId> permission) throws CoreException, InvalidRepoException, InvalidGroupException, InvalidUserException {
        repoService.validateRepoId(repoId);
        if (!groupExists(repoId, groupId)) {
            throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
        }
        return repoService.getRepo(repoId).getDatabase().removePermissionFromGroup(repoId, groupId, permission);
    }
}
