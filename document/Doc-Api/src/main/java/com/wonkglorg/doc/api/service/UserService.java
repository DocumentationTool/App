package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
public class UserService{
	
	private final RepoService repoService;
	
	public UserService(@Lazy RepoService repoService) {this.repoService = repoService;}
	
	public UpdateDatabaseResponse createUser(String username, String password) {
	return null;
	}
	
	public UserProfile getUser(String username, String password) {
		return null;
	}
	
	public UpdateDatabaseResponse deleteUser(String username) {
		return null;
	}
	
}