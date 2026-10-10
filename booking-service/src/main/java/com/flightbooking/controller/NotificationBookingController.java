package com.flightbooking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightbooking.dto.BookingDetails;
import com.flightbooking.service.BookingService;

@RestController
@RequestMapping("/api/bookings/internal/notifications")
public class NotificationBookingController {

	 private final BookingService bookingService;

	    public NotificationBookingController(BookingService bookingService) {
	        this.bookingService = bookingService;
	    }

	    @GetMapping("/bookings/{bookingReference}")
	    public ResponseEntity<BookingDetails> getBookingForNotification(
	            @PathVariable String bookingReference,
	            @RequestHeader(value = "X-Internal-Service", required = false) String internalService
	    ) {

	        if (!"NOTIFICATION-SERVICE".equals(internalService)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	        }

	        return ResponseEntity.ok(
	                bookingService.getBookingDetails(bookingReference)
	        );
	    }
}
