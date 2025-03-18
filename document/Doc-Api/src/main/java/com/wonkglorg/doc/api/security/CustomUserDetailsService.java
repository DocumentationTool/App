package com.wonkglorg.doc.api.security;

import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;

/**
 *
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private UserAuthenticationManager authManager;

	public CustomUserDetailsService(UserAuthenticationManager authManager) {
		this.authManager = authManager;
	}

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		UserId userId = UserId.of(id);
		if (DEV_MODE) {
			return new User(userId.id(), "password_hash",
					List.of(new SimpleGrantedAuthority("ROLE_USER"),
					new SimpleGrantedAuthority("ROLE_ADMIN")));
		}


		UserProfile user = authManager.loadByUserId(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		List<GrantedAuthority> authorities =
				user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.roleID().id()))
						.collect(Collectors.toList());

		//todo:jmd add back password hash
		return new User(user.getId().id(), "", authorities);
	}
}

