package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.exception.LoginFailedException;
import com.wonkglorg.doc.api.security.JwtUtil;
import com.wonkglorg.doc.api.security.UserAuthenticationManager;
import com.wonkglorg.doc.api.security.UserAuthenticationManager.AuthResponse;
import com.wonkglorg.doc.api.security.UserAuthenticationManager.LoginRequest;
import com.wonkglorg.doc.core.objects.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.AUTH;

@RestController
@RequestMapping(AUTH)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserAuthenticationManager authManager;

    public AuthController(UserAuthenticationManager authManager) {
        this.authManager = authManager;
    }

    //@formatter:off
	@Operation(
			summary = "Test login endpoint",
			description = "Returns a token when DEV_MODE is enabled, otherwise returns a 403."
	)
	@GetMapping("/login")
	public ResponseEntity<AuthResponse> login() {
		log.info("Login GET request received");
		return new ResponseEntity<>(new AuthResponse(null, "Endpoint for testing purposes only enable DEV_MODE!"), HttpStatus.FORBIDDEN);
	}
	@Operation(
			summary = "User logout",
			description = "Logs out the user."
	)
	@GetMapping("/logout")
	public ResponseEntity<String> logout() {
		log.info("Logout request received");
		return ResponseEntity.notFound().build();
	}
	@Operation(
			summary = "User login",
			description = "Attempts to login a user with the given credentials. Returns a token if successful, otherwise returns a 401."
	)
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Parameter(description = "The user's id and password.")
			@RequestBody LoginRequest request) {
		log.info("Login POST request received");
		try{
			authManager.authenticate(UserId.of(request.userId()), request.password());
			
			String token = JwtUtil.generateToken(request.userId());
			return ResponseEntity.ok(new AuthResponse(token, null));
		} catch(LoginFailedException e){
			return new ResponseEntity<>(new AuthResponse(null, e.getMessage()), e.getStatusCode());
		}
	}
	//@formatter:on

}
