package com.flightbooking.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.flightbooking.notification.dto.BookingDetails;

@Component
public class BookingServiceClient {

	private final RestClient restClient;

	public BookingServiceClient(
			@Value("${booking-service.url}") String bookingServiceUrl) {
		this.restClient = RestClient.builder()
							.baseUrl(bookingServiceUrl).build();
	}

	public BookingDetails getBooking(String bookingReference) {
		return restClient.get()
						.uri("/api/bookings/internal/notifications/bookings/{bookingReference}",
								bookingReference)
							.header("X-Internal-Service", "NOTIFICATION-SERVICE")
							.retrieve().body(BookingDetails.class);
	}
}
