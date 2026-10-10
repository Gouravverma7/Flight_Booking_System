package com.flightbooking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationServiceClient {
	   private final RestClient restClient;

	   public NotificationServiceClient(
			   	@Value("${notification-service.url}")
	            String notificationServiceUrl) {

	        this.restClient = RestClient.builder()
	                .baseUrl(notificationServiceUrl)
	                .build();
	    }

	    public void sendBookingConfirmation(
	            String bookingReference) {

	        restClient.post()
	                .uri(
	                        "/api/notifications/internal/booking-confirmation/{bookingReference}",
	                        bookingReference
	                )
	                .header(
	                        "X-Internal-Service",
	                        "PAYMENT-SERVICE"
	                )
	                .retrieve()
	                .toBodilessEntity();
	    }
}
