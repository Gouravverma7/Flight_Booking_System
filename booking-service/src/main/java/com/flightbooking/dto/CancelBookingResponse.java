package com.flightbooking.dto;

public record CancelBookingResponse(
		String bookingReference,
	    String status,
	    String message) {

}
