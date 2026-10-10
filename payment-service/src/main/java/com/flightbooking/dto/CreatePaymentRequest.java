package com.flightbooking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {

	 @NotBlank(message = "Booking reference is required")
	 private String bookingReference;
}
