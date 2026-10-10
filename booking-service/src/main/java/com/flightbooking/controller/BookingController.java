package com.flightbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.flightbooking.dto.BookingDetails;
import com.flightbooking.dto.BookingResponse;
import com.flightbooking.dto.CreateBookingRequest;
import com.flightbooking.dto.PassengerDetails;
import com.flightbooking.dto.PaymentDetails;
import com.flightbooking.service.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(
            BookingService bookingService
    ) {
        this.bookingService = bookingService;
    }

    // CREATE BOOKING

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            Authentication authentication,
            @Valid @RequestBody CreateBookingRequest request
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bookingService.createBooking(
                                userId,
                                request
                        )
                );
    }

    // MY BOOKINGS

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                bookingService.getMyBookings(userId)
        );
    }

    // BOOKING DETAILS

    @GetMapping("/{bookingReference}")
    public ResponseEntity<BookingDetails> getBooking(
            Authentication authentication,
            @PathVariable String bookingReference
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                bookingService.getMyBookingDetails(
                        userId,
                        bookingReference
                )
        );
    }

    // PASSENGERS

    @GetMapping("/{bookingReference}/passengers")
    public ResponseEntity<List<PassengerDetails>> getPassengers(
            Authentication authentication,
            @PathVariable String bookingReference
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                bookingService.getPassengers(
                        userId,
                        bookingReference
                )
        );
    }

    // PAYMENT SUCCESS
    // INTERNAL

    @PostMapping(
            "/internal/bookings/{bookingReference}/payment-success"
    )
    public ResponseEntity<Void> paymentSuccess(
            @PathVariable String bookingReference,
            @RequestHeader("X-Internal-Service")
            String internalService,
            @RequestHeader("X-Payment-Intent-Id")
            String paymentIntentId
    ) {

        if (!"PAYMENT-SERVICE".equals(internalService)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        bookingService.confirmPayment(
                bookingReference,
                paymentIntentId
        );

        return ResponseEntity.ok().build();
    }

    // TICKET SERVICE
    // INTERNAL

    @GetMapping(
            "/internal/bookings/{bookingReference}"
    )
    public ResponseEntity<BookingDetails> getBookingForTicket(
            @PathVariable String bookingReference,
            @RequestHeader("X-Internal-Service")
            String internalService
    ) {

        if (!"TICKET-SERVICE".equals(internalService)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
                bookingService.getBookingDetails(
                        bookingReference
                )
        );
    }

    // PAYMENT DETAILS
    // INTERNAL

    @GetMapping(
            "/internal/bookings/{bookingReference}/payment-details"
    )
    public ResponseEntity<PaymentDetails> getPaymentDetails(
            @PathVariable String bookingReference,
            @RequestHeader("X-Internal-Service")
            String internalService
    ) {

        if (!"PAYMENT-SERVICE".equals(internalService)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
                bookingService.getPaymentDetails(
                        bookingReference
                )
        );
    }

    // JWT USER ID

    private Long getUserId(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof Long userId) {
            return userId;
        }

        if (principal instanceof String userId) {
            return Long.valueOf(userId);
        }

        throw new IllegalStateException(
                "Authenticated user ID is not available"
        );
    }

}
