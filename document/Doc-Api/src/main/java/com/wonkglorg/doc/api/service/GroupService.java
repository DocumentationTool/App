package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.user.Group;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
public class GroupService implements GroupCalls {
    private final RepoService repoService;

    public GroupService(@Lazy RepoService repoService) {
        this.repoService = repoService;
    }


    public boolean groupExists(RepoId repoId, GroupId groupId) throws InvalidRepoException {
        repoService.validateRepoId(repoId);
        return repoService.getRepo(repoId).getDatabase().groupExists(groupId);
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
}
