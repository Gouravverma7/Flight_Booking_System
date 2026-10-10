package com.flightbooking.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBookingRequest {
	@NotBlank(message = "Offer ID is required")
	private String offerId;

	@NotBlank(message = "Currency is required")
	@Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter ISO code")
	private String currency;

	@NotEmpty(message = "At least one passenger is required")
	@Size(max = 9, message = "Maximum 9 passengers are allowed")
	@Valid
	private List<PassengerRequest> passengers;

	public CreateBookingRequest() {
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public List<PassengerRequest> getPassengers() {
		return passengers;
	}

	public void setPassengers(List<PassengerRequest> passengers) {
		this.passengers = passengers;
	}

}
