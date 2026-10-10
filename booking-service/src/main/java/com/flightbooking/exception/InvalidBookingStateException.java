package com.flightbooking.exception;

public class InvalidBookingStateException extends RuntimeException{
	public InvalidBookingStateException(String message) {
        super(message);
    }
}
