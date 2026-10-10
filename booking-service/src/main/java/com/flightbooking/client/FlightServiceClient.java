package com.flightbooking.client;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class FlightServiceClient {

    private final RestClient restClient;

    public FlightServiceClient(
            @Value("${flight-service.url}") String flightServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(flightServiceUrl)
                .build();
    }

    // --------------------------------------------------
    // GET OFFER
    // --------------------------------------------------

    public Map<String, Object> getOffer(String offerId) {
        validateOfferId(offerId);
        return execute("Get flight offer", () ->
                restClient.get()
                        .uri("/api/flights/offers/{offerId}", offerId)
                        .retrieve()
                        .body(Map.class));
    }

    // --------------------------------------------------
    // REPRICE OFFER
    // --------------------------------------------------

    public Map<String, Object> repriceOffer(String offerId) {
        validateOfferId(offerId);
        return execute("Reprice flight offer", () ->
                restClient.post()
                        .uri("/api/flights/offers/{offerId}/price", offerId)
                        .retrieve()
                        .body(Map.class));
    }

    // --------------------------------------------------
    // CURRENCY CONVERSION
    // --------------------------------------------------

    public Map<String, Object> convertCurrency(
            BigDecimal amount,
            String from,
            String to) {

        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Amount must be zero or greater");
        }

        if (from == null || from.isBlank()
                || to == null || to.isBlank()) {
            throw new IllegalArgumentException(
                    "Source and target currencies are required");
        }

        return execute("Currency conversion", () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/flights/currency/convert")
                                .queryParam("amount", amount)
                                .queryParam("from", from)
                                .queryParam("to", to)
                                .build())
                        .retrieve()
                        .body(Map.class));
    }

    // --------------------------------------------------
    // CREATE DUFFEL ORDER
    // --------------------------------------------------

    public Map<String, Object> createOrder(Map<String, Object> request) {
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException(
                    "Flight order request is required");
        }

        return execute("Create flight order", () ->
                restClient.post()
                        .uri("/api/flights/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(Map.class));
    }

    // --------------------------------------------------
    // SHARED HTTP ERROR HANDLING
    // --------------------------------------------------

    private Map<String, Object> execute(String operation, RequestSupplier requestSupplier) {
        try {
            Map<String, Object> response = requestSupplier.get();

            if (response == null) {
                throw new IllegalStateException(
                        operation + " returned an empty response");
            }

            return response;

        } catch (RestClientResponseException e) {

            String responseBody = e.getResponseBodyAsString();
            System.err.println(operation + " failed");
            System.err.println("HTTP status: " + e.getStatusCode());
            System.err.println("Response: " + responseBody);

            if (responseBody != null
                    && responseBody.toLowerCase().contains(
                            "offer_no_longer_available")) {

                throw new IllegalStateException(
                        "FLIGHT_OFFER_UNAVAILABLE: "
                                + "This flight offer is no longer available. "
                                + "Search again and select a fresh offer.",
                        e);
            }

            throw new IllegalStateException(
                    operation + " failed with HTTP "
                            + e.getStatusCode().value()
                            + ": " + responseBody,
                    e);

        } catch (ResourceAccessException e) {

            System.err.println(
                    operation + " failed: flight-service is unreachable");

            throw new IllegalStateException(
                    "Flight service is unavailable. Please retry later.",
                    e);
        }
    }

    private void validateOfferId(String offerId) {

        if (offerId == null || offerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Flight offer ID is required");
        }
    }

    @FunctionalInterface
    private interface RequestSupplier {
        Map<String, Object> get();
    }
}

