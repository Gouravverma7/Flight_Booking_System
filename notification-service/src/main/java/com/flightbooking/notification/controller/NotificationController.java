package com.flightbooking.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightbooking.notification.dto.SendTicketEmailRequest;
import com.flightbooking.notification.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@PostMapping("/booking-confirmation/{bookingReference}")
	public ResponseEntity<String> sendBookingConfirmation(
			@PathVariable String bookingReference,
			@Valid @RequestBody SendTicketEmailRequest request){
		notificationService.sendBookingConfirmation(bookingReference, request.email());
		return ResponseEntity.ok("Booking confirmation email sent successfully");
	}


	@PostMapping("/internal/booking-confirmation/{bookingReference}")
	public ResponseEntity<String> sendAutomaticBookingConfirmation(
	        @PathVariable String bookingReference,
	        @RequestHeader(
	                value = "X-Internal-Service",
	                required = false
	        ) String internalService) {

	    if (!"PAYMENT-SERVICE".equals(internalService)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
	    }

	    notificationService.sendAutomaticBookingConfirmation(
	            bookingReference
	    );

	    return ResponseEntity.ok(
	            "Booking confirmation notification processed successfully"
	    );
	}

}
