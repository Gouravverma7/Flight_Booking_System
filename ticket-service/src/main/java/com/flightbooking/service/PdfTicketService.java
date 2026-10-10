package com.flightbooking.service;

import java.io.File;
import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.flightbooking.dto.BookingDetails;
import com.flightbooking.dto.PassengerDetails;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfTicketService {

	 	private final String storagePath;
	    public PdfTicketService(@Value("${ticket.storage-path}") String storagePath) {
	        this.storagePath = storagePath;
	    }

	    public File generateTicket(BookingDetails booking) throws Exception {

	        File directory = new File(storagePath);
	        if (!directory.exists() && !directory.mkdirs()) {
	            throw new IllegalStateException(
	                    "Unable to create ticket storage directory: "
	                            + directory.getAbsolutePath()
	            );
	        }


	        validateBookingData(booking);
	        String fileName = "ticket-"
	                        + booking.bookingReference()
	                        + ".pdf";

	        File file = new File(directory, fileName);

	        Document document = new Document(
	                        PageSize.A4,
	                        36,
	                        36,
	                        36,
	                        36
	                );

	        try {
	            PdfWriter.getInstance(document, new FileOutputStream(file));
	            document.open();
	            addHeader(document, booking);
	            addFlightInformation(document, booking);
	            addPassengerInformation(document, booking);
	            addPaymentInformation(document, booking);
	            addFooter(document);
	        } finally {
	            if (document.isOpen()) {
	                document.close();
	            }
	        }
	        return file;
	    }

	    private void validateBookingData(BookingDetails booking) {
	        if (booking == null) {
	            throw new IllegalArgumentException("Booking details cannot be null");
	        }

	        if (isBlank(booking.bookingReference())) {
	            throw new IllegalStateException("Booking reference is missing");
	        }

	        if (isBlank(booking.airline())) {
	            throw new IllegalStateException("Airline information is missing");
	        }

	        if (isBlank(booking.origin())) {
	            throw new IllegalStateException("Origin is missing");
	        }

	        if (isBlank(booking.destination())) {
	            throw new IllegalStateException("Destination is missing");
	        }

	        if (isBlank(booking.departureTime())) {
	            throw new IllegalStateException("Departure time is missing");
	        }

	        if (isBlank(booking.arrivalTime())) {
	            throw new IllegalStateException("Arrival time is missing");
	        }

	        if (booking.passengers() == null || booking.passengers().isEmpty()) {
	            throw new IllegalStateException("Passenger information is missing");
	        }
	    }

	    private boolean isBlank(String value) {
	        return value == null || value.isBlank();
	    }

	    private void addHeader(Document document, BookingDetails booking) throws DocumentException {
	        Font titleFont = FontFactory.getFont(
	                        FontFactory.HELVETICA_BOLD,
	                        20
	                );

	        Paragraph title = new Paragraph(
	                        "FLIGHT BOOKING CONFIRMATION",
	                        titleFont
	                );

	        title.setAlignment(Element.ALIGN_CENTER);

	        document.add(title);

	        document.add(new Paragraph(" "));

	        Font referenceFont = FontFactory.getFont(
	                        FontFactory.HELVETICA_BOLD,
	                        12
	                );

	        Paragraph reference =
	                new Paragraph(
	                        "Booking Reference: "
	                                + booking.bookingReference(),
	                        referenceFont
	                );

	        reference.setAlignment(
	                Element.ALIGN_CENTER
	        );

	        document.add(reference);

	        document.add(
	                new Paragraph(" ")
	        );
	    }

	    private void addFlightInformation(
	            Document document,
	            BookingDetails booking
	    ) throws DocumentException {

	        addSectionTitle(
	                document,
	                "Flight Information"
	        );

	        PdfPTable table =
	                new PdfPTable(2);

	        table.setWidthPercentage(100);

	        addRow(
	                table,
	                "Airline",
	                booking.airline()
	        );

	        addRow(
	                table,
	                "From",
	                booking.origin()
	        );

	        addRow(
	                table,
	                "To",
	                booking.destination()
	        );

	        addRow(
	                table,
	                "Departure",
	                booking.departureTime()
	        );

	        addRow(
	                table,
	                "Arrival",
	                booking.arrivalTime()
	        );

	        addRow(
	                table,
	                "Duffel Order",
	                booking.duffelOrderId()
	        );

	        addRow(
	                table,
	                "Airline Reference",
	                booking.airlineBookingReference()
	        );

	        document.add(table);

	        document.add(
	                new Paragraph(" ")
	        );
	    }

	    private void addPassengerInformation(
	            Document document,
	            BookingDetails booking
	    ) throws DocumentException {

	        addSectionTitle(
	                document,
	                "Passenger Information"
	        );

	        PdfPTable table =
	                new PdfPTable(4);

	        table.setWidthPercentage(100);

	        table.addCell("Name");
	        table.addCell("DOB");
	        table.addCell("Nationality");
	        table.addCell("Passport");

	        for (
	                PassengerDetails passenger
	                : booking.passengers()
	        ) {

	            String passengerName =
	                    safeValue(
	                            passenger.firstName()
	                    )
	                    + " "
	                    + safeValue(
	                            passenger.lastName()
	                    );

	            table.addCell(
	                    passengerName.trim()
	            );

	            table.addCell(
	                    passenger.dateOfBirth() == null
	                            ? "N/A"
	                            : passenger.dateOfBirth().toString()
	            );

	            table.addCell(
	                    safeValue(
	                            passenger.nationality()
	                    )
	            );

	            table.addCell(
	                    safeValue(
	                            passenger.passportNumber()
	                    )
	            );
	        }

	        document.add(table);

	        document.add(
	                new Paragraph(" ")
	        );
	    }

	    private void addPaymentInformation(
	            Document document,
	            BookingDetails booking
	    ) throws DocumentException {

	        addSectionTitle(
	                document,
	                "Payment Information"
	        );

	        PdfPTable table =
	                new PdfPTable(2);

	        table.setWidthPercentage(100);

	        String paymentAmount =
	                booking.paymentAmount() == null
	                        ? booking.totalAmount().toString()
	                        : booking.paymentAmount().toString();

	        String paymentCurrency =
	                booking.paymentCurrency() == null
	                        || booking.paymentCurrency().isBlank()
	                        ? booking.currency()
	                        : booking.paymentCurrency();

	        addRow(
	                table,
	                "Amount",
	                paymentAmount
	                        + " "
	                        + paymentCurrency
	        );

	        addRow(
	                table,
	                "Payment Status",
	                "PAID"
	        );

	        addRow(
	                table,
	                "Booking Status",
	                booking.status()
	        );

	        document.add(table);

	        document.add(
	                new Paragraph(" ")
	        );
	    }

	    private void addSectionTitle(
	            Document document,
	            String title
	    ) throws DocumentException {

	        Font font =
	                FontFactory.getFont(
	                        FontFactory.HELVETICA_BOLD,
	                        14
	                );

	        document.add(
	                new Paragraph(
	                        title,
	                        font
	                )
	        );

	        document.add(
	                new Paragraph(" ")
	        );
	    }

	    private void addRow(
	            PdfPTable table,
	            String key,
	            String value
	    ) {

	        table.addCell(key);

	        table.addCell(
	                safeValue(value)
	        );
	    }

	    private String safeValue(
	            String value
	    ) {

	        if (value == null
	                || value.isBlank()) {

	            return "N/A";
	        }

	        return value;
	    }

	    private void addFooter(
	            Document document
	    ) throws DocumentException {

	        document.add(
	                new Paragraph(" ")
	        );

	        Paragraph footer =
	                new Paragraph(
	                        "Thank you for booking with us. "
	                                + "Please carry a valid identity "
	                                + "document during your journey."
	                );

	        footer.setAlignment(
	                Element.ALIGN_CENTER
	        );

	        document.add(footer);
	    }
































}
