package com.flightbooking.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}