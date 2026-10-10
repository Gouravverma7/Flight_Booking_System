package com.flightbooking.dto;

import java.time.LocalDate;

public record PassengerDetails(
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		String gender,
		String nationality,
		String email,
		String phone,
		String passportNumber
		) {

}
