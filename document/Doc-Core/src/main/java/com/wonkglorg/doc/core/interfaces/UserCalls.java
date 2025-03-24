package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;

import java.util.List;

public interface UserCalls{
	
	/**
	 * Creates a new user in the database
	 *
	 * @param user the user to create
	 * @return the response
	 */
	boolean addUser(UserProfile user) throws ClientException, CoreSqlException;
	
	/**
	 * Removes a user from the database
	 *
	 * @param userId the user to remove
	 * @return the response
	 * @throws CoreSqlException if the user could not be removed
	 * @throws InvalidUserException if the user is invalid
	 */
	boolean removeUser(UserId userId) throws CoreSqlException, InvalidUserException;
	
	/**
	 * Gets all users
	 *
	 * @return a list of users
	 */
	List<UserProfile> getUsers();
	
	/**
	 * Gets a user from the database
	 *
	 * @param userId the id of the user
	 * @return the user
	 * @throws InvalidUserException if the user is invalid
	 */
	UserProfile getUser(UserId userId) throws InvalidUserException;
	
	/**
	 * Checks if a user exists in the database
	 *
	 * @param userId the user to check
	 * @return weather it exists or not
	 */
	boolean userExists(UserId userId);
	
	/**
	 * Sets the role of a user in a repo
	 *
	 * @param userId the user
	 * @param role the role
	 */
	void addRole(UserId userId, Role role) throws InvalidRepoException, InvalidUserException;
	
	/**
	 * Removes the role from a user
	 *
	 * @param userId the user
	 * @param role the role
	 */
	void removeRole(UserId userId, Role role) throws InvalidUserException, InvalidRepoException;
}
