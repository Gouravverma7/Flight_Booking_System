package com.flightbooking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentServiceClient {
    private final RestClient restClient;

    public PaymentServiceClient(@Value("${payment-service.url}") String paymentServiceUrl) {
        this.restClient = RestClient.builder()
                         	.baseUrl(paymentServiceUrl).build();
    }

    public void refundBooking(String bookingReference) {
        restClient.post()
                .uri(
                        "/api/payments/internal/refund/{bookingReference}",
                        bookingReference
                )
                .header(
                        "X-Internal-Service",
                        "BOOKING-SERVICE"
                )
                .retrieve()
                .toBodilessEntity();
    }
}
