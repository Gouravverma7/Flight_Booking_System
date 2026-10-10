package com.flightbooking.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookingDetails(
		  String bookingReference,
	      Long userId,
	      String offerId,
	      String airline,
	      String origin,
	      String destination,
	      String departureTime,
	      String arrivalTime,

	        // Original Duffel fare
	      BigDecimal totalAmount,
	      String currency,

	        // Customer payment fare
	      BigDecimal paymentAmount,
	      String paymentCurrency,
	      BigDecimal exchangeRate,

	        // Booking status
	      String status,

	      String duffelOrderId,
	      String airlineBookingReference,
	      List<PassengerDetails> passengers

		) {


}
