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
@RequestMapping("/internal/bookings")
public class InternalBookingController {
	private final BookingService bookingService;

    public InternalBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingReference}")
    public ResponseEntity<BookingDetails> getBookingDetails(
            @PathVariable String bookingReference,
            @RequestHeader("X-Internal-Service") String internalService
    ) {

        if (!"TICKET-SERVICE".equals(internalService)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                bookingService.getBookingDetails(bookingReference)
        );
    }

//     NOTIFICATION SERVICE

    @GetMapping("/notifications/bookings/{bookingReference}")
    public ResponseEntity<BookingDetails> getBookingForNotification(
            @PathVariable String bookingReference,
            @RequestHeader("X-Internal-Service") String internalService
    ) {

        if (!"NOTIFICATION-SERVICE".equals(internalService)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                bookingService.getBookingDetails(bookingReference)
        );
    }
}
