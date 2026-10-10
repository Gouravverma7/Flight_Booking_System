package com.flightbooking.flight.dto;

public record DuffelOrderResponse(
		String orderId,
        String bookingReference,
        String totalAmount,
        String currency) {

}
