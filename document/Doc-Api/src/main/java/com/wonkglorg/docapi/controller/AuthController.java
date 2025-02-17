package com.wonkglorg.docapi.controller;

import static com.wonkglorg.docapi.controller.Constants.ControllerPaths.AUTH;
import com.wonkglorg.docapi.exception.LoginFailedException;
import com.wonkglorg.docapi.security.JwtUtil;
import com.wonkglorg.docapi.security.UserAuthenticationManager;
import com.wonkglorg.docapi.security.UserAuthenticationManager.AuthResponse;
import com.wonkglorg.docapi.security.UserAuthenticationManager.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public ResponseEntity<String> login() {
		log.info("Login GET request received");
		return ResponseEntity.ok("Login");
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
