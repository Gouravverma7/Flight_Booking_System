package com.flightbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.flightbooking.client.FlightServiceClient;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.BookingStatus;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PassengerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private FlightServiceClient flightServiceClient;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        bookingService =
                new BookingService(
                        bookingRepository,
                        passengerRepository,
                        flightServiceClient
                );
    }

    @Test
    void shouldFindBookingByUser() {

        Booking booking = new Booking();

        booking.setBookingReference("FBTEST12345");
        booking.setUserId(1L);
        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        when(
                bookingRepository
                        .findByBookingReferenceAndUserId(
                                "FBTEST12345",
                                1L
                        )
        ).thenReturn(
                Optional.of(booking)
        );

        var result =
                bookingService.getMyBookingDetails(
                        1L,
                        "FBTEST12345"
                );

        assertEquals(
                "FBTEST12345",
                result.bookingReference()
        );
    }
}