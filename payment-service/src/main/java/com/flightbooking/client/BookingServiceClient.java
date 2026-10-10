package com.flightbooking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.flightbooking.dto.PaymentDetails;

@Component
public class BookingServiceClient {
	private final RestClient restClient;

	public BookingServiceClient(
			@Value("${booking-service.url}") String bookingServiceUrl) {
		this.restClient = RestClient.builder().baseUrl(bookingServiceUrl).build();
	}

	public void confirmPayment(
	        String bookingReference,
	        String paymentIntentId) {

	    restClient.post()
	            .uri("/api/bookings/internal/bookings/{bookingReference}/payment-success",
	                    bookingReference)
	            .header("X-Internal-Service", "PAYMENT-SERVICE")
	            .header("X-Payment-Intent-Id", paymentIntentId)
	            .retrieve()
	            .toBodilessEntity();
	}

	   public PaymentDetails getPaymentDetails(
	            String bookingReference) {

	        return restClient.get()
	                .uri(
	                        "/api/bookings/internal/bookings/{bookingReference}/payment-details",
	                        bookingReference
	                )
	                .header(
	                        "X-Internal-Service",
	                        "PAYMENT-SERVICE"
	                )
	                .retrieve()
	                .body(PaymentDetails.class);
	    }
}

