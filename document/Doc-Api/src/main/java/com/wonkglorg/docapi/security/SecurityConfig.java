package com.wonkglorg.docapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
	/**
	 * Configures the security filter chain.
	 *
	 * @param http the {@link HttpSecurity} object used to configure security.
	 * @param authManager the {@link AuthenticationManager} to handle authentication.
	 * @return the configured {@link SecurityFilterChain}.
	 * @throws Exception if an error occurs during configuration.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)  //disables cross-site request forgery since JWT is stateless
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //no session management we use jwt
			.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/login") //permits everyone to the login endpoint regardless of role
											   .permitAll().anyRequest().authenticated())// Require authentication for all other endpoints
			.addFilterBefore(new JwtAuthenticationFilter(),
					UsernamePasswordAuthenticationFilter.class); // Add JWT filter before default authentication filter
		return http.build();
	}
	
	/**
	 * Configures the authentication manager.
	 * <p>
	 * This method sets up a {@link DaoAuthenticationProvider} that:
	 * <br>
	 * - Uses a {@link UserDetailsService} for loading user details.
	 * <br>
	 * - Uses a {@link BCryptPasswordEncoder} for password hashing.
	 *
	 * @param userDetailsService the service to retrieve user authentication details.
	 * @return the configured {@link AuthenticationManager}.
	 */
	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(new BCryptPasswordEncoder()); // Secure password hashing
		return new ProviderManager(authProvider);
	}
}

