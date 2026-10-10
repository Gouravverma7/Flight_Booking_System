package com.flightbooking.notification.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.flightbooking.notification.dto.BookingDetails;
import com.flightbooking.notification.dto.PassengerDetails;
import com.flightbooking.notification.entity.Notification;
import com.flightbooking.notification.entity.NotificationStatus;
import com.flightbooking.notification.repository.NotificationRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

	private static final String BOOKING_CONFIRMATION =
								"BOOKING_CONFIRMATION";
	private final JavaMailSender mailSender;
	private final NotificationRepository notificationRepository;

	public EmailNotificationService(
			JavaMailSender mailSender,
			NotificationRepository notificationRepository) {
		this.mailSender = mailSender;
		this.notificationRepository = notificationRepository;
	}

	  public void sendBookingConfirmation(
	            BookingDetails booking,
	            String recipientEmail,
	            byte[] ticketPdf) {

	        Notification notification =
	                notificationRepository
	                        .findByBookingReferenceAndNotificationType(
	                                booking.bookingReference(),
	                                BOOKING_CONFIRMATION
	                        )
	                        .orElseGet(Notification::new);

	        if (notification.getStatus()
	                == NotificationStatus.SENT) {

	            return;
	        }

	        notification.setBookingReference(
	                booking.bookingReference()
	        );

	        notification.setRecipientEmail(
	                recipientEmail
	        );

	        notification.setNotificationType(
	                BOOKING_CONFIRMATION
	        );

	        notification.setStatus(
	                NotificationStatus.PENDING
	        );

	        notification.setCreatedAt(
	                LocalDateTime.now()
	        );

	        notificationRepository.save(notification);

	        try {

	            MimeMessage message =
	                    mailSender.createMimeMessage();

	            MimeMessageHelper helper =
	                    new MimeMessageHelper(
	                            message,
	                            true,
	                            StandardCharsets.UTF_8.name()
	                    );

	            helper.setTo(recipientEmail);

	            helper.setSubject(
	                    "Flight Booking Confirmed - "
	                            + booking.bookingReference()
	            );

	            helper.setText(
	                    buildHtmlEmail(booking),
	                    true
	            );

	            helper.addAttachment(
	                    "ticket-"
	                            + booking.bookingReference()
	                            + ".pdf",
	                    new ByteArrayResource(ticketPdf)
	            );

	            mailSender.send(message);

	            notification.setStatus(
	                    NotificationStatus.SENT
	            );

	            notification.setSentAt(
	                    LocalDateTime.now()
	            );

	            notification.setErrorMessage(null);

	            notificationRepository.save(
	                    notification
	            );

	        } catch (Exception ex) {

	            notification.setStatus(
	                    NotificationStatus.FAILED
	            );

	            notification.setErrorMessage(
	                    ex.getMessage()
	            );

	            notificationRepository.save(
	                    notification
	            );

	            throw new RuntimeException(
	                    "Failed to send booking confirmation email",
	                    ex
	            );
	        }
	    }

	    private String buildHtmlEmail(
	            BookingDetails booking) {

	        String passengerHtml = "";

	        if (booking.passengers() != null) {

	            for (PassengerDetails passenger :
	                    booking.passengers()) {

	                passengerHtml += """
	                        <tr>
	                            <td>%s %s</td>
	                            <td>%s</td>
	                            <td>%s</td>
	                        </tr>
	                        """.formatted(
	                        safe(passenger.firstName()),
	                        safe(passenger.lastName()),
	                        safe(passenger.nationality()),
	                        safe(passenger.email())
	                );
	            }
	        }

	        String paymentAmount =
	                booking.paymentAmount() != null
	                        ? booking.paymentAmount().toString()
	                        : booking.totalAmount().toString();

	        String paymentCurrency =
	                booking.paymentCurrency() != null
	                        && !booking.paymentCurrency().isBlank()
	                        ? booking.paymentCurrency()
	                        : booking.currency();

	        String exchangeRate =
	                booking.exchangeRate() != null
	                        ? booking.exchangeRate().toString()
	                        : "N/A";

	        return """
	                <!DOCTYPE html>
	                <html>
	                <head>
	                    <meta charset="UTF-8">

	                    <style>
	                        body {
	                            font-family: Arial, sans-serif;
	                            background: #f4f6f8;
	                            margin: 0;
	                            padding: 30px;
	                        }

	                        .container {
	                            max-width: 700px;
	                            margin: auto;
	                            background: white;
	                            border-radius: 10px;
	                            padding: 30px;
	                        }

	                        .header {
	                            text-align: center;
	                            padding-bottom: 20px;
	                            border-bottom: 1px solid #ddd;
	                        }

	                        .title {
	                            font-size: 24px;
	                            font-weight: bold;
	                        }

	                        .success {
	                            margin-top: 15px;
	                            font-size: 18px;
	                            font-weight: bold;
	                        }

	                        .section {
	                            margin-top: 25px;
	                        }

	                        table {
	                            width: 100%%;
	                            border-collapse: collapse;
	                            margin-top: 10px;
	                        }

	                        th, td {
	                            border: 1px solid #ddd;
	                            padding: 10px;
	                            text-align: left;
	                        }

	                        th {
	                            background: #f1f1f1;
	                        }

	                        .booking-reference {
	                            font-size: 20px;
	                            font-weight: bold;
	                        }

	                        .footer {
	                            margin-top: 30px;
	                            font-size: 13px;
	                            color: #777;
	                            text-align: center;
	                        }
	                    </style>
	                </head>

	                <body>

	                    <div class="container">

	                        <div class="header">

	                            <div class="title">
	                                Flight Booking Confirmation
	                            </div>

	                            <div class="success">
	                                Your booking has been confirmed successfully.
	                            </div>

	                        </div>

	                        <div class="section">

	                            <p>
	                                Booking Reference:
	                            </p>

	                            <div class="booking-reference">
	                                %s
	                            </div>

	                        </div>

	                        <div class="section">

	                            <h3>Flight Details</h3>

	                            <table>

	                                <tr>
	                                    <th>Airline</th>
	                                    <td>%s</td>
	                                </tr>

	                                <tr>
	                                    <th>Route</th>
	                                    <td>%s → %s</td>
	                                </tr>

	                                <tr>
	                                    <th>Departure</th>
	                                    <td>%s</td>
	                                </tr>

	                                <tr>
	                                    <th>Arrival</th>
	                                    <td>%s</td>
	                                </tr>

	                                <tr>
	                                    <th>Airline Booking Reference</th>
	                                    <td>%s</td>
	                                </tr>

	                            </table>

	                        </div>

	                        <div class="section">

	                            <h3>Passengers</h3>

	                            <table>

	                                <tr>
	                                    <th>Name</th>
	                                    <th>Nationality</th>
	                                    <th>Email</th>
	                                </tr>

	                                %s

	                            </table>

	                        </div>

	                        <div class="section">

	                            <h3>Payment</h3>

	                            <p>
	                                Amount Paid:
	                                <strong>%s %s</strong>
	                            </p>

	                            <p>
	                                Original Fare:
	                                <strong>%s %s</strong>
	                            </p>

	                            <p>
	                                Exchange Rate:
	                                <strong>%s</strong>
	                            </p>

	                            <p>
	                                Payment Status:
	                                <strong>PAID</strong>
	                            </p>

	                            <p>
	                                Booking Status:
	                                <strong>%s</strong>
	                            </p>

	                        </div>

	                        <div class="section">

	                            <p>
	                                Your flight ticket is attached
	                                to this email.
	                            </p>

	                            <p>
	                                Please keep this email and the
	                                attached ticket for your journey.
	                            </p>

	                        </div>

	                        <div class="footer">

	                            Thank you for booking with us.

	                            <br><br>

	                            This is an automated email.
	                            Please do not reply directly to this message.

	                        </div>

	                    </div>

	                </body>
	                </html>
	                """.formatted(
	                safe(booking.bookingReference()),
	                safe(booking.airline()),
	                safe(booking.origin()),
	                safe(booking.destination()),
	                safe(booking.departureTime()),
	                safe(booking.arrivalTime()),
	                safe(booking.airlineBookingReference()),
	                passengerHtml,
	                paymentAmount,
	                paymentCurrency,
	                safe(booking.totalAmount()),
	                safe(booking.currency()),
	                exchangeRate,
	                safe(booking.status())
	        );
	    }

	    private String safe(Object value) {

	        return value == null
	                ? "N/A"
	                : value.toString();
	    }







}
