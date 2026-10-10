package com.flightbooking.service;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flightbooking.client.BookingServiceClient;
import com.flightbooking.dto.BookingDetails;
import com.flightbooking.entity.Ticket;
import com.flightbooking.entity.TicketStatus;
import com.flightbooking.repository.TicketRepository;

@Service
public class TicketService {
	 private final TicketRepository ticketRepository;
	    private final BookingServiceClient bookingServiceClient;
	    private final PdfTicketService pdfTicketService;

	    public TicketService(
	            TicketRepository ticketRepository,
	            BookingServiceClient bookingServiceClient,
	            PdfTicketService pdfTicketService
	    ) {
	        this.ticketRepository = ticketRepository;
	        this.bookingServiceClient = bookingServiceClient;
	        this.pdfTicketService = pdfTicketService;
	    }

	    @Transactional
	    public Ticket generateTicket(
	            String bookingReference
	    ) throws Exception {

	        BookingDetails booking =
	                bookingServiceClient.getBooking(
	                        bookingReference
	                );
	        if (!"CONFIRMED".equals(booking.status())) {

	            throw new IllegalStateException(
	                    "Ticket can only be generated "
	                            + "for confirmed booking"
	            );
	        }
	        if (booking.departureTime() == null
	                || booking.departureTime().isBlank()) {

	            throw new IllegalStateException(
	                    "Booking departure time is missing"
	            );
	        }

	        if (booking.arrivalTime() == null
	                || booking.arrivalTime().isBlank()) {

	            throw new IllegalStateException(
	                    "Booking arrival time is missing"
	            );
	        }
	        System.out.println(
	                "========== TICKET SERVICE BOOKING =========="
	        );

	        System.out.println(
	                "Booking Reference: "
	                        + booking.bookingReference()
	        );

	        System.out.println(
	                "Origin: "
	                        + booking.origin()
	        );

	        System.out.println(
	                "Destination: "
	                        + booking.destination()
	        );

	        System.out.println(
	                "Departure Time: "
	                        + booking.departureTime()
	        );

	        System.out.println(
	                "Arrival Time: "
	                        + booking.arrivalTime()
	        );

	        System.out.println(
	                "Duffel Order ID: "
	                        + booking.duffelOrderId()
	        );

	        System.out.println(
	                "Airline Reference: "
	                        + booking.airlineBookingReference()
	        );

	        System.out.println(
	                "============================================"
	        );
	        File pdf =
	                pdfTicketService.generateTicket(
	                        booking
	                );
	        Ticket ticket =
	                ticketRepository
	                        .findByBookingReference(
	                                bookingReference
	                        )
	                        .orElseGet(
	                                Ticket::new
	                        );
	        ticket.setBookingReference(
	                booking.bookingReference()
	        );

	        ticket.setDuffelOrderId(
	                booking.duffelOrderId()
	        );

	        ticket.setAirlineBookingReference(
	                booking.airlineBookingReference()
	        );

	        ticket.setPdfFileName(
	                pdf.getName()
	        );

	        ticket.setPdfFilePath(
	                pdf.getAbsolutePath()
	        );

	        ticket.setStatus(
	                TicketStatus.GENERATED
	        );
	        return ticketRepository.save(ticket);
	    }

	    @Transactional(readOnly = true)
	    public Ticket getTicket(
	            String bookingReference
	    ) {

	        return ticketRepository
	                .findByBookingReference(
	                        bookingReference
	                )
	                .orElseThrow(
	                        () -> new IllegalStateException(
	                                "Ticket Not Found"
	                        )
	                );
	    }

























}
