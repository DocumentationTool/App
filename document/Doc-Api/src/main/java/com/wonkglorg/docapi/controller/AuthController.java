package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.security.AuthResponse;
import com.wonkglorg.docapi.security.AuthenticationManager;
import com.wonkglorg.docapi.security.JwtUtil;
import com.wonkglorg.docapi.security.LoginRequest;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


	private AuthenticationManager authManager;

	public AuthController(AuthenticationManager authManager) {
		this.authManager = authManager;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {

		UserProfile profile =
				authManager.authenticate(request.userId(), request.password()).orElseThrow();

		String token = JwtUtil.generateToken(request.userId());
		return ResponseEntity.ok(new AuthResponse(token));
	}
}
