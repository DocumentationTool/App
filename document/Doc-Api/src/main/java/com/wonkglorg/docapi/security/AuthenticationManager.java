package com.wonkglorg.docapi.security;

import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationManager {

	public record AuthResponse(String token) {
	}

	public record LoginRequest(String userId, String password) {
	}

	public AuthenticationManager() {
	}

	/**
	 * Authenticates the user
	 *
	 * @param userId the users id
	 * @param password the users password
	 * @return true if valid false otherwise
	 */
	public Optional<UserProfile> authenticate(final String userId, final String password) {
		return Optional.empty();
	}

	/**
	 * Checks if the give userId is valid
	 *
	 * @param userId the user id to check
	 * @return true if it exists false otherwise
	 */
	public boolean exists(final String userId) {
		return true;
	}

}
