package com.flightbooking.flight.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class FlightSearchRequest {

	@NotNull(message = "Origin is required")
	@Pattern(regexp = "[A-Za-z]{3}",
			message = "Origin must be a 3-letter IATA code")
	private String origin;

	@NotNull(message = "Origin is required")
	@Pattern(regexp = "[A-Za-z]{3}",
			message = "Destination must be a 3-letter IATA code")
	private String destination;

	@FutureOrPresent(message = "Departure date must be today or future")
	private LocalDate departureDate;

	@Min(value = 1, message = "At least 1 adult in required")
	@Max(value = 9, message = "Maximum 9 adults allowed")
	private int adults = 1;

	private String cabin = "economy";

    /**
     * Currency selected by the customer.
     * Default is INR.
     */
    private String currency = "INR";


}
