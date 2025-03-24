package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.db.UserDatabase;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidGroupException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.interfaces.GroupCalls;
import com.wonkglorg.doc.core.interfaces.UserCalls;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.Group;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Component
@Service
public class UserService implements UserCalls, GroupCalls{
	
	private final UserDatabase userDatabase = new UserDatabase(Path.of("users.db"));
	private final PermissionService permissionService;
	private final RepoService repoService;
	
	public UserService(@Lazy PermissionService permissionService, @Lazy RepoService repoService) {
		this.permissionService = permissionService;
		this.repoService = repoService;
	}
	
	//---- User ----
	
	@Override
	public boolean addUser(UserProfile user) throws ClientException, CoreSqlException {
		if(userExists(user.getId())){
			throw new ClientException("User with id '%s' already exists".formatted(user.getId()));
		}
		
		for(GroupId groupId : user.getGroups()){
			if(!groupExists(groupId)){
				throw new ClientException("Group with id '%s' does not exist".formatted(groupId));
			}
		}
		return userDatabase.addUser(user);
		
	}
	
	@Override
	public boolean removeUser(UserId userId) throws InvalidUserException, CoreSqlException {
		validateUser(userId);
		
		//removes all related permissions from any repo referencing this user
		for(var repo : repoService.getRepositories().values()){
			repo.getDatabase().permissionFunctions().cleanUpUser(userId);
		}
		
		return userDatabase.removeUser(userId);
	}
	
	@Override
	public List<UserProfile> getUsers() {
		return userDatabase.getUsers();
	}
	
	@Override
	public UserProfile getUser(UserId userId) throws InvalidUserException {
		return userDatabase.getUser(userId);
	}
	
	@Override
	public boolean userExists(UserId userId) {
		try{
			UserProfile users = userDatabase.getUser(userId);
			return users != null;
		} catch(InvalidUserException e){
			return false;
		}
	}
	
	/**
	 * Validates if a user exists
	 *
	 * @param userId the user id
	 */
	public void validateUser(UserId userId) throws InvalidUserException {
		if(!userExists(userId)){
			throw new InvalidUserException("User '%s' does not exist".formatted(userId));
		}
	}
	
	public void validateGroup(GroupId groupId) throws InvalidGroupException {
		if(!groupExists(groupId)){
			throw new InvalidGroupException("Group '%s' does not exist".formatted(groupId));
		}
	}
	
	@Override
	public boolean groupExists(GroupId groupId) {
		return userDatabase.groupExists(groupId);
	}
	
	@Override
	public boolean userInGroup(GroupId groupId, UserId userId) throws InvalidUserException, InvalidGroupException {
		validateUser(userId);
		validateGroup(groupId);
		return userDatabase.userInGroup(groupId, userId);
	}
	
	@Override
	public boolean addGroup(Group group) throws CoreException, InvalidGroupException {
		if(groupExists(group.getId())){
			throw new InvalidGroupException("Group with id '%s' already exists!".formatted(group.getId()));
		}
		return userDatabase.addGroup(group);
	}
	
	@Override
	public boolean removeGroup(GroupId groupId) throws InvalidGroupException {
		validateGroup(groupId);
		
		for(var repo : repoService.getRepositories().values()){
			repo.getDatabase().permissionFunctions().cleanUpGroup(groupId);
		}
		
		return userDatabase.removeGroup(groupId);
	}
	
	@Override
	public List<Group> getGroups() {
		return userDatabase.getGroups();
	}
	
	@Override
	public Group getGroup(GroupId groupId) throws InvalidGroupException {
		validateGroup(groupId);
		return userDatabase.getGroup(groupId);
	}
	
	@Override
	public Group renameGroup(GroupId groupId, String newName) throws CoreException, InvalidGroupException {
		validateGroup(groupId);
		return userDatabase.renameGroup(groupId, newName);
	}
	
	@Override
	public boolean addUserToGroup(GroupId groupId, UserId userId) throws CoreException, ClientException {
		
		if(!groupExists(groupId)){
			throw new InvalidGroupException("Group with id '%s' does not exist".formatted(groupId));
		}
		validateUser(userId);
		
		if(userInGroup(groupId, userId)){
			throw new ClientException("User with id '%s' is already in group '%s'".formatted(userId, groupId));
		}
		
		return userDatabase.addUserToGroup(groupId, userId);
	}
	
	@Override
	public boolean removeUserFromGroup(GroupId groupId, UserId userId) throws CoreException, InvalidGroupException, InvalidUserException {
		validateGroup(groupId);
		
		validateUser(userId);
		
		if(!userInGroup(groupId, userId)){
			throw new InvalidUserException("User with id '%s' is not in group '%s'".formatted(userId, groupId));
		}
		
		return userDatabase.removeUserFromGroup(groupId, userId);
	}
	
	@Override
	public Set<UserProfile> getUsersFromGroup(GroupId groupId) throws InvalidGroupException {
		validateGroup(groupId);
		return userDatabase.getUsersFromGroup(groupId);
	}
	
	@Override
	public Set<Group> getGroupsFromUser(UserId userId) throws InvalidUserException {
		validateUser(userId);
		return userDatabase.getGroupsFromUser(userId);
	}
	
	public UserDatabase getUserDatabase() {
		return userDatabase;
	}
	
	@Override
	public void addRole(UserId userId, Role role) throws InvalidRepoException, InvalidUserException {
		validateUser(userId);
		userDatabase.addRole(userId, role);
	}
	
	@Override
	public void removeRole(UserId userId, Role role) throws InvalidUserException, InvalidRepoException {
		validateUser(userId);
		userDatabase.removeRole(userId, role);
	}
}
