package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.List;

public interface UserCalls{
	
	boolean addUser(RepoId repoId, UserProfile user);
	
	boolean removeUser(RepoId repoId, UserId userId);
	
	/**
	 * Gets all users in a repo
	 *
	 * @param repoId the repo to get the users from
	 * @param userId the user to get the users from
	 * @return a list of users
	 */
	List<UserProfile> getUsers(RepoId repoId, UserId userId);
}
