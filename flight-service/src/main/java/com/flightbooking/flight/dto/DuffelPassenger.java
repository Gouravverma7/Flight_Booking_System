package com.flightbooking.flight.dto;

public record DuffelPassenger(
		String id,
		String given_name,
        String family_name,
        String born_on,
        String gender,
        String title,
        String email,
        String phone_number) {

}
