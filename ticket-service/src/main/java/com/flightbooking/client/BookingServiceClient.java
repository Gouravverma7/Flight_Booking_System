package com.flightbooking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbooking.dto.BookingDetails;

@Component
public class BookingServiceClient {
	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public BookingServiceClient(@Value("${booking-service.url}") String bookingServiceUrl, ObjectMapper objectMapper) {
		this.restClient = RestClient.builder().baseUrl(bookingServiceUrl).build();
		this.objectMapper = objectMapper;
	}

	public BookingDetails getBooking(String bookingReference) {

		System.out.println();
		System.out.println("==========================================");
		System.out.println("TICKET SERVICE -> BOOKING SERVICE");
		System.out.println("Booking Reference: " + bookingReference);
		System.out.println("==========================================");

		String rawResponse = restClient.get().uri(
				"/api/bookings/internal/bookings/{bookingReference}", bookingReference)
				.header("X-Internal-Service", "TICKET-SERVICE").retrieve().body(String.class);

		System.out.println();
		System.out.println("========== RAW BOOKING RESPONSE ==========");
		System.out.println(rawResponse);
		System.out.println("==========================================");

		try {

			JsonNode json = objectMapper.readTree(rawResponse);

			System.out.println();
			System.out.println("========== BOOKING FIELDS ==========");

			System.out.println("bookingReference = " + json.path("bookingReference").asText());

			System.out.println("origin = " + json.path("origin").asText());

			System.out.println("destination = " + json.path("destination").asText());

			System.out.println("departureTime = " + json.path("departureTime").asText());

			System.out.println("arrivalTime = " + json.path("arrivalTime").asText());

			System.out.println("status = " + json.path("status").asText());

			System.out.println("duffelOrderId = " + json.path("duffelOrderId").asText());

			System.out.println("====================================");

			BookingDetails booking = objectMapper.readValue(rawResponse, BookingDetails.class);

			System.out.println();
			System.out.println("========== DESERIALIZED BOOKING ==========");

			System.out.println("Booking Reference: " + booking.bookingReference());

			System.out.println("Origin: " + booking.origin());

			System.out.println("Destination: " + booking.destination());

			System.out.println("Departure Time: " + booking.departureTime());

			System.out.println("Arrival Time: " + booking.arrivalTime());

			System.out.println("Status: " + booking.status());

			System.out.println("==========================================");

			return booking;

		} catch (Exception e) {

			throw new IllegalStateException("Failed to parse booking response from Booking Service", e);
		}
	}
}
