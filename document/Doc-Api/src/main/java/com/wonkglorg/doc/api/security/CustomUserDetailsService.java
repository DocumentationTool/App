package com.wonkglorg.doc.api.security;

import com.wonkglorg.doc.api.service.UserService;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
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

    private final UserService userService;
    private UserAuthenticationManager authManager;

    public CustomUserDetailsService(UserAuthenticationManager authManager, UserService userService) {
        this.authManager = authManager;
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UserId userId = UserId.of(id);
        if (DEV_MODE) {
            return new User(userId.id(), "password_hash",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN")));
        }


        //todo:jmd this won't work how to properly handle that 1 locale cache for users? instead of per repo? otherwise this won't work but probably just leave it
		
		UserProfile user = null;
		try{
			user = userService.getUser(userId);
		} catch(InvalidUserException e){
			throw new UsernameNotFoundException("User not found", e);
		}
		
		List<GrantedAuthority> authorities =
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList());

        //todo:jmd add back password hash
        return new User(user.getId().id(), "", authorities);
    }
}

