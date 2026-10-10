package com.flightbooking.notification.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.flightbooking.notification.client.BookingServiceClient;
import com.flightbooking.notification.client.TicketServiceClient;
import com.flightbooking.notification.dto.BookingDetails;
import com.flightbooking.notification.dto.PassengerDetails;

@Service
public class NotificationService {

	 private final BookingServiceClient bookingServiceClient;
	 private final TicketServiceClient ticketServiceClient;
	 private final EmailNotificationService emailNotificationService;

	 public NotificationService(BookingServiceClient bookingServiceClient,
			 				TicketServiceClient ticketServiceClient,
			 				EmailNotificationService emailNotificationService) {
	    this.bookingServiceClient = bookingServiceClient;
	    this.ticketServiceClient = ticketServiceClient;
	    this.emailNotificationService = emailNotificationService;
	}

	 public void sendBookingConfirmation(String bookingReference, String recipientEmail) {

		 // 1. Get latest booking details
		 BookingDetails booking = bookingServiceClient.getBooking(bookingReference);

		 if(booking == null) {
			 throw new IllegalStateException("Booking not found: " + bookingReference);
		 }

		 //2. Booking must be confirmed
		 if(!"CONFIRMED".equalsIgnoreCase(booking.status())) {
			 throw new IllegalStateException("Booking is not confirmed. Current status: " + booking.status());
		 }

		 // 3. Generate / refresh ticket
		 ticketServiceClient.generateTicket(bookingReference);

		 // 4. Download genearted PDF
		 byte[] ticketPdf = ticketServiceClient.downloadTicket(bookingReference);

		 if(ticketPdf == null || ticketPdf.length == 0) {
			 throw new IllegalStateException("Ticket PDF is empty for booking: " + bookingReference);
		 }

		 // 5. Send email + PDF
		 emailNotificationService.sendBookingConfirmation(booking, recipientEmail, ticketPdf);
	 }

	 public void sendAutomaticBookingConfirmation(
		        String bookingReference) {

		    BookingDetails booking =
		            bookingServiceClient.getBooking(bookingReference);

		    if (booking == null) {
		        throw new IllegalStateException(
		                "Booking not found: " + bookingReference
		        );
		    }

		    if (!"CONFIRMED".equalsIgnoreCase(booking.status())) {
		        throw new IllegalStateException(
		                "Booking is not confirmed: " + bookingReference
		        );
		    }

		    if (booking.passengers() == null ||
		            booking.passengers().isEmpty()) {

		        throw new IllegalStateException(
		                "No passenger found for booking: "
		                        + bookingReference
		        );
		    }

		    String recipientEmail =
		            booking.passengers()
		                    .stream()
		                    .map(PassengerDetails::email)
		                    .filter(Objects::nonNull)
		                    .map(String::trim)
		                    .filter(email -> !email.isBlank())
		                    .findFirst()
		                    .orElseThrow(() ->
		                            new IllegalStateException(
		                                    "Passenger email not found for booking: "
		                                            + bookingReference
		                            )
		                    );

		    ticketServiceClient.generateTicket(bookingReference);

		    byte[] ticketPdf =
		            ticketServiceClient.downloadTicket(
		                    bookingReference
		            );

		    if (ticketPdf == null || ticketPdf.length == 0) {
		        throw new IllegalStateException(
		                "Ticket PDF generation failed: "
		                        + bookingReference
		        );
		    }

		    emailNotificationService.sendBookingConfirmation(
		            booking,
		            recipientEmail,
		            ticketPdf
		    );
		}


}
