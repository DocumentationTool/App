package com.wonkglorg.doc.api.security;

import com.wonkglorg.doc.api.exception.LoginFailedException;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserAuthenticationManager{
	
	public record AuthResponse(String token, String error){}
	
	public record LoginRequest(String userId, String password){}
	
	public UserAuthenticationManager() {
	}
	
	/**
	 * Authenticates the user
	 *
	 * @param userId the users id
	 * @param password the users password
	 * @return true if valid false otherwise
	 */
	public Optional<UserProfile> authenticate(final UserId userId, final String password) throws LoginFailedException {
		return Optional.empty();
	}
	
	/**
	 * THIS SHOULD NOT BE USED TO AUTHENTICATE USERS! This is used to access user profiles from existing users with authentication
	 *
	 * @param userId the user id to load
	 * @return the user profile if it exists
	 */
	public Optional<UserProfile> loadByUserId(final UserId userId) {
		return Optional.empty();
	}
	
	/**
	 * Checks if the give userId is valid
	 *
	 * @param userId the user id to check
	 * @return true if it exists false otherwise
	 */
	public boolean exists(final UserId userId) {
		return true;
	}
	
}
