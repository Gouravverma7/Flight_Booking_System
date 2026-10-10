package com.flightbooking.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PassengerRequest {

	@NotBlank(message = "Title is required")
	private String title;

	@NotBlank(message = "First name is required")
	private String firstName;

	@NotBlank(message = "Last name is required")
	private String lastName;

	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be in the past")
	private LocalDate dateOfBirth;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotBlank(message = "Nationality is required")
	private String nationality;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email address")
	private String email;

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^[0-9+()\\- ]{7,20}$", message = "Invalid phone number")
	private String phone;

	private String passportNumber;

	@NotBlank(message = "Passenger type is required")
	private String passengerType;

	public PassengerRequest() {
	}
}
