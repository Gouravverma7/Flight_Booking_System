package com.flightbooking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Please enter a valid email")
	private String email;

	@NotBlank(message = "Password is required")
	private String password;




}
