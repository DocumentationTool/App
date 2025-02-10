package com.wonkglorg.docapi.controller;

import static com.wonkglorg.docapi.controller.Constants.AuthMappings.LOGIN;
import static com.wonkglorg.docapi.controller.Constants.ControllerPaths.AUTH;
import com.wonkglorg.docapi.exception.LoginFailedException;
import com.wonkglorg.docapi.security.JwtUtil;
import com.wonkglorg.docapi.security.UserAuthenticationManager;
import com.wonkglorg.docapi.security.UserAuthenticationManager.AuthResponse;
import com.wonkglorg.docapi.security.UserAuthenticationManager.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AUTH)
public class AuthController{
	
	private final UserAuthenticationManager authManager;
	
	public AuthController(UserAuthenticationManager authManager) {
		this.authManager = authManager;
	}
	
	@GetMapping(LOGIN)
	public ResponseEntity<String> login() {
		return ResponseEntity.ok("Login");
	}
	
	@PostMapping(LOGIN)
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
		try{
			authManager.authenticate(request.userId(), request.password());
			
			String token = JwtUtil.generateToken(request.userId());
			return ResponseEntity.ok(new AuthResponse(token, null));
		} catch(LoginFailedException e){
			return new ResponseEntity<>(new AuthResponse(null, e.getMessage()), e.getStatusCode());
		}
	}
	
}
