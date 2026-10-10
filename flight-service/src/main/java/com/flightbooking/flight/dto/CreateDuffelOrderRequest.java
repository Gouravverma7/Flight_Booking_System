package com.flightbooking.flight.dto;

import java.util.List;

public record CreateDuffelOrderRequest(
        String offerId,
        String currency,
        String amount,
        List<DuffelPassenger> passengers,
        String bookingReference) {
}
