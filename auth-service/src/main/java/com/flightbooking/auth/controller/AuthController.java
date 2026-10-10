package com.flightbooking.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.flightbooking.auth.dto.AuthResponse;
import com.flightbooking.auth.dto.LoginRequest;
import com.flightbooking.auth.dto.LoginResponse;
import com.flightbooking.auth.dto.RefreshTokenRequest;
import com.flightbooking.auth.dto.RegisterRequest;
import com.flightbooking.auth.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
		AuthResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
		LoginResponse response =  authService.login(request);
		return ResponseEntity.ok(response);
	}


	@GetMapping("/me")
	public ResponseEntity<String> me(Authentication authentication){
		return ResponseEntity.ok("Authenticated user: " + authentication.getName());
	}


	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
		LoginResponse response = authService.refreshToken(request.getRefreshToken());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request){
		authService.logout(request.getRefreshToken());
		return ResponseEntity.noContent().build();
	}

}
