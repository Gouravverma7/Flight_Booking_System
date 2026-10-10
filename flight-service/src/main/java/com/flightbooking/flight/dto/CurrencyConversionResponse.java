package com.flightbooking.flight.dto;

public record CurrencyConversionResponse(
		String date,
        String base,
        String quote,
        double rate) {

}
