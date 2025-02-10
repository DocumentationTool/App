package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.exception.LoginFailedException;
import com.wonkglorg.docapi.security.UserAuthenticationManager;
import com.wonkglorg.docapi.security.UserAuthenticationManager.AuthResponse;
import com.wonkglorg.docapi.security.UserAuthenticationManager.LoginRequest;
import com.wonkglorg.docapi.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController{
	
	private final UserAuthenticationManager authManager;
	
	public AuthController(UserAuthenticationManager authManager) {
		this.authManager = authManager;
	}
	
	@PostMapping("/login")
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
