package com.wonkglorg.docapi.security;

import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 *
 */
@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	private UserAuthenticationManager authManager;
	
	public CustomUserDetailsService(UserAuthenticationManager authManager) {
		this.authManager = authManager;
	}
	
	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		UserProfile user = authManager.loadByUserId(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new User(user.getUsername(), user.getPasswordHash(), new ArrayList<>());
	}
}

