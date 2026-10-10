package com.flightbooking.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendTicketEmailRequest(
		@NotBlank
		@Email
		String email

		) {

}
