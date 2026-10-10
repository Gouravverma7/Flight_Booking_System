package com.flightbooking.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TicketServiceClient {

	private final RestClient restClient;

	public TicketServiceClient(
			@Value("${ticket-service.url}") String ticketServiceUrl) {
		this.restClient = RestClient.builder().baseUrl(ticketServiceUrl).build();
	}

	public void generateTicket(String bookingReference) {
		restClient.post().uri("/api/tickets/{bookingReference}/generate", bookingReference)
					.retrieve().toBodilessEntity();

	}

	public byte[] downloadTicket(String bookingReference) {
		return restClient.get().uri("/api/tickets/{bookingReference}/download", bookingReference)
						.retrieve().body(byte[].class);
	}
}
