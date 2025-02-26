package com.wonkglorg.doc.api.security.filters;

import com.wonkglorg.doc.api.security.CustomUserDetailsService;
import com.wonkglorg.doc.api.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final CustomUserDetailsService customUserDetailsService;
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	// Service to load user details from DB

	public JwtAuthenticationFilter(CustomUserDetailsService customUserDetailsService) {
		this.customUserDetailsService = customUserDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {

		if (DEV_MODE) {
			log.info("DEV_MODE: Bypassing JWT authentication");
			// Load the user details from the username
			User userDetails = (User) customUserDetailsService.loadUserByUsername("dev_p10209");

			// Create an Authentication object and set it in the SecurityContext
			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

			// Set the authentication in the SecurityContext
			SecurityContextHolder.getContext().setAuthentication(authentication);
			chain.doFilter(request, response);
			return;
		}
		log.info("JWT Authentication Filter");

		// Extract token from the Authorization header
		String token = request.getHeader("Authorization");

		// Check if the token is present and starts with "Bearer "
		if (token != null && token.startsWith("Bearer ")) {
			token = token.substring(7); // Remove the "Bearer " prefix

			String username = JwtUtil.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				// Validate the token (you can check expiration, signature, etc.)
				if (JwtUtil.validateToken(token)) {
					// Load the user details from the username
					User userDetails = (User) customUserDetailsService.loadUserByUsername(username);

					// Create an Authentication object and set it in the SecurityContext
					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(userDetails, null,
									userDetails.getAuthorities());

					// Set the authentication in the SecurityContext
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}

		// Continue with the filter chain
		chain.doFilter(request, response);
	}
}
