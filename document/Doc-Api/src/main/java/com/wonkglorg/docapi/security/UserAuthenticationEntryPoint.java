package com.wonkglorg.docapi.security;

import com.wonkglorg.docapi.controller.AdminController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Defines response logic for unauthorized access to pages without proper token in {@link SecurityConfig}
 */
@Component
public class UserAuthenticationEntryPoint implements AuthenticationEntryPoint {

    	private static final Logger log = LoggerFactory.getLogger(UserAuthenticationEntryPoint.class);
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.info("Unauthenticated request to: " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
