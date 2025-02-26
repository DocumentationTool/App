package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;
import static com.wonkglorg.doc.api.DocApiApplication.DEV_USER;
import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.AUTH;
import com.wonkglorg.doc.api.exception.LoginFailedException;
import com.wonkglorg.doc.api.security.JwtUtil;
import com.wonkglorg.doc.api.security.UserAuthenticationManager;
import com.wonkglorg.doc.api.security.UserAuthenticationManager.AuthResponse;
import com.wonkglorg.doc.api.security.UserAuthenticationManager.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AUTH)
public class AuthController{
	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	private final UserAuthenticationManager authManager;
	
	public AuthController(UserAuthenticationManager authManager) {
		this.authManager = authManager;
	}
	
	@GetMapping("/login")
	public ResponseEntity<AuthResponse> login() {
		log.info("Login GET request received");
		if(DEV_MODE){
			authManager.authenticate(DEV_USER.getId(), "TEST_PASSWORT");
			String token = JwtUtil.generateToken(DEV_USER.getId());
			return ResponseEntity.ok(new AuthResponse(token, null));
		}
		
		return new ResponseEntity<>(new AuthResponse(null, "Endpoint for testing purposes only enable DEV_MODE!"), HttpStatus.FORBIDDEN);
	}
	
	@GetMapping("/logout")
	public ResponseEntity<String> logout() {
		log.info("Logout request received");
		return ResponseEntity.notFound().build();
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
		log.info("Login POST request received");
		try{
			authManager.authenticate(request.userId(), request.password());
			
			String token = JwtUtil.generateToken(request.userId());
			return ResponseEntity.ok(new AuthResponse(token, null));
		} catch(LoginFailedException e){
			return new ResponseEntity<>(new AuthResponse(null, e.getMessage()), e.getStatusCode());
		}
	}
	
}
