package com.flightbooking.auth.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {

	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private Long userId;
	private String name;
	private String email;
	private String role;

	public LoginResponse(String accessToken, String refreshToken, Long userId, String name,
			String email, String role) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.tokenType = "Bearer";
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
	}





}
