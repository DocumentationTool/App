package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.user.Group;

import java.util.List;

/**
 * A common interface referencing all group related calls
 */
public interface GroupCalls{
	
	boolean addGroup(RepoId repoId, Group group) throws InvalidRepoException, CoreException;
	
	boolean removeGroup(RepoId repoId, GroupId groupId) throws CoreException, InvalidRepoException;
	
	/**
	 * Gets all groups in a repo
	 * @param repoId the repo to get the groups from
	 * @param groupId the group to get the groups from
	 * @return a list of groups
	 */
	List<Group> getGroups(RepoId repoId, GroupId groupId);
	
}
