package com.flightbooking.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flightbooking.client.FlightServiceClient;
import com.flightbooking.dto.BookingDetails;
import com.flightbooking.dto.BookingResponse;
import com.flightbooking.dto.CancelBookingRequest;
import com.flightbooking.dto.CancelBookingResponse;
import com.flightbooking.dto.CreateBookingRequest;
import com.flightbooking.dto.PassengerDetails;
import com.flightbooking.dto.PassengerRequest;
import com.flightbooking.dto.PaymentDetails;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.BookingStatus;
import com.flightbooking.entity.Passenger;
import com.flightbooking.exception.BookingNotFoundException;
import com.flightbooking.exception.BookingOwnershipException;
import com.flightbooking.exception.InvalidBookingStateException;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PassengerRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final FlightServiceClient flightServiceClient;

    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            FlightServiceClient flightServiceClient) {

        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.flightServiceClient = flightServiceClient;
    }

    // 1. CREATE BOOKING

    @Transactional
    @SuppressWarnings("unchecked")
    public BookingResponse createBooking(
            Long userId,
            CreateBookingRequest request) {

        if (request == null
                || request.getOfferId() == null
                || request.getOfferId().isBlank()) {
            throw new IllegalArgumentException(
                    "Flight offer ID is required");
        }

        if (request.getCurrency() == null
                || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException(
                    "Payment currency is required");
        }

        if (request.getPassengers() == null
                || request.getPassengers().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one passenger is required");
        }

        // Retrieve/reprice the selected offer.
        Map<String, Object> offer =
                flightServiceClient.repriceOffer(request.getOfferId());

        if (offer == null) {
            throw new IllegalStateException(
                    "FLIGHT_OFFER_UNAVAILABLE: Unable to retrieve current offer");
        }

        String offerId = stringValue(offer.get("offerId"));
        if (offerId == null) {
            offerId = request.getOfferId();
        }

        String airline = stringValue(offer.get("airline"));
        String price = stringValue(offer.get("price"));
        String currency = stringValue(offer.get("currency"));

        if (price == null || currency == null) {
            throw new IllegalStateException(
                    "FLIGHT_OFFER_UNAVAILABLE: Price or currency is missing");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(price);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Flight provider returned an invalid price", ex);
        }

        if (amount.signum() <= 0) {
            throw new IllegalStateException(
                    "Flight provider returned a non-positive price");
        }

        Object segmentsObject = offer.get("segments");

        if (!(segmentsObject instanceof List<?> segments)
                || segments.isEmpty()) {
            throw new IllegalStateException(
                    "Flight offer has no segments");
        }

        Object firstObject = segments.get(0);
        Object lastObject = segments.get(segments.size() - 1);

        if (!(firstObject instanceof Map<?, ?> firstSegment)
                || !(lastObject instanceof Map<?, ?> lastSegment)) {
            throw new IllegalStateException(
                    "Flight provider returned invalid segment data");
        }

        String departureTime =
                getRequiredFlightTime(firstSegment, "departureTime");

        String arrivalTime =
                getRequiredFlightTime(lastSegment, "arrivalTime");

        String origin = stringValue(firstSegment.get("origin"));
        String destination = stringValue(lastSegment.get("destination"));

        if (origin == null || destination == null) {
            throw new IllegalStateException(
                    "Flight origin or destination is missing");
        }

        Booking booking = new Booking();

        booking.setBookingReference(generateBookingReference());
        booking.setUserId(userId);
        booking.setOfferId(offerId);
        booking.setAirline(airline);
        booking.setOrigin(origin);
        booking.setDestination(destination);
        booking.setDepartureTime(departureTime);
        booking.setArrivalTime(arrivalTime);

        String flightNumber = stringValue(firstSegment.get("flightNumber"));
        if (flightNumber != null) {
            booking.setFlightNumber(flightNumber);
        }

        // Original flight price and currency from provider.
        booking.setTotalAmount(amount);
        booking.setCurrency(currency);
        booking.setOriginalAmount(amount);
        booking.setOriginalCurrency(currency);

        // Convert to the currency selected by the customer.
        String selectedCurrency = request.getCurrency().trim()
                .toUpperCase();

        Map<String, Object> conversion =
                flightServiceClient.convertCurrency(
                        amount,
                        currency,
                        selectedCurrency);

        if (conversion == null
                || conversion.get("convertedAmount") == null
                || conversion.get("rate") == null) {
            throw new IllegalStateException(
                    "Currency conversion failed");
        }

        BigDecimal paymentAmount =
                new BigDecimal(
                        String.valueOf(conversion.get("convertedAmount")));

        BigDecimal exchangeRate =
                new BigDecimal(
                        String.valueOf(conversion.get("rate")));

        if (paymentAmount.signum() <= 0
                || exchangeRate.signum() <= 0) {
            throw new IllegalStateException(
                    "Invalid currency conversion result");
        }

        booking.setPaymentAmount(paymentAmount);
        booking.setPaymentCurrency(selectedCurrency);
        booking.setExchangeRate(exchangeRate);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        booking = bookingRepository.saveAndFlush(booking);

        savePassengers(booking, request);

        return mapToBookingResponse(booking);
    }

    // 2. FLIGHT SEGMENT VALIDATION

    private String getRequiredFlightTime(
            Map<?, ?> segment,
            String field) {

        String result = stringValue(segment.get(field));

        if (result == null
                || "n/a".equalsIgnoreCase(result)
                || "na".equalsIgnoreCase(result)
                || "-".equals(result)) {
            throw new IllegalStateException(
                    "Flight offer is missing valid " + field);
        }

        return result;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }

        String result = String.valueOf(value).trim();

        if (result.isBlank()
                || "null".equalsIgnoreCase(result)) {
            return null;
        }

        return result;
    }

    // 3. SAVE PASSENGERS

    private void savePassengers(
            Booking booking,
            CreateBookingRequest request) {

        for (PassengerRequest passengerRequest : request.getPassengers()) {
            Passenger passenger = new Passenger();

            passenger.setBooking(booking);
            passenger.setFirstName(passengerRequest.getFirstName());
            passenger.setLastName(passengerRequest.getLastName());
            passenger.setDateOfBirth(passengerRequest.getDateOfBirth());
            passenger.setGender(passengerRequest.getGender());
            passenger.setNationality(passengerRequest.getNationality());
            passenger.setEmail(passengerRequest.getEmail());
            passenger.setPhone(passengerRequest.getPhone());
            passenger.setPassportNumber(
                    passengerRequest.getPassportNumber());

            passengerRepository.save(passenger);
        }
    }

    private String generateBookingReference() {
        return "FB" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }

    // 4. CONFIRM PAYMENT AND CREATE AIRLINE ORDER

    @Transactional
    public void confirmPayment(
            String bookingReference,
            String paymentIntentId) {

        Booking booking = bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalStateException(
                        "Booking not found: " + bookingReference));

        // Idempotency for an already-confirmed booking.
        if (booking.getStatus() == BookingStatus.CONFIRMED
                && booking.getDuffelOrderId() != null
                && !booking.getDuffelOrderId().isBlank()) {
            return;
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT
                && booking.getStatus() != BookingStatus.PAYMENT_PROCESSING
                && booking.getStatus() != BookingStatus.FAILED) {
            throw new IllegalStateException(
                    "Cannot process booking " + bookingReference
                            + " in status " + booking.getStatus());
        }

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe PaymentIntent ID is required");
        }

        booking.setStatus(BookingStatus.PAYMENT_PROCESSING);
        bookingRepository.saveAndFlush(booking);

        try {
            List<Passenger> passengers =
                    passengerRepository.findByBookingId(booking.getId());

            if (passengers == null || passengers.isEmpty()) {
                throw new IllegalStateException(
                        "No passengers found for " + bookingReference);
            }

            List<Map<String, Object>> passengerData =
                    passengers.stream()
                            .map(passenger -> {
                                Map<String, Object> data = new HashMap<>();

                                data.put("id", passenger.getId());
                                data.put("given_name", passenger.getFirstName());
                                data.put("family_name", passenger.getLastName());

                                if (passenger.getDateOfBirth() == null) {
                                    throw new IllegalStateException(
                                            "Passenger date of birth is missing");
                                }

                                data.put(
                                        "born_on",
                                        passenger.getDateOfBirth().toString());

                                String gender = passenger.getGender();

                                if (gender == null || gender.isBlank()) {
                                    throw new IllegalStateException(
                                            "Passenger gender is missing");
                                }

                                data.put(
                                        "gender",
                                        gender.substring(0, 1).toLowerCase());

                                data.put("title", "mr");
                                data.put("email", passenger.getEmail());
                                data.put("phone_number", passenger.getPhone());
                                data.put("type", "adult");

                                return data;
                            })
                            .toList();

            Map<String, Object> orderRequest = new HashMap<>();

            orderRequest.put("offerId", booking.getOfferId());
            orderRequest.put(
                    "bookingReference",
                    booking.getBookingReference());
            orderRequest.put(
                    "currency",
                    booking.getOriginalCurrency());
            orderRequest.put(
                    "amount",
                    booking.getOriginalAmount().toPlainString());
            orderRequest.put("passengers", passengerData);

            Map<String, Object> order =
                    flightServiceClient.createOrder(orderRequest);

            if (order == null
                    || order.get("orderId") == null
                    || order.get("bookingReference") == null) {
                throw new IllegalStateException(
                        "Duffel returned an incomplete order response");
            }

            booking.setDuffelOrderId(
                    String.valueOf(order.get("orderId")));

            booking.setAirlineBookingReference(
                    String.valueOf(order.get("bookingReference")));

            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.saveAndFlush(booking);

        } catch (RuntimeException ex) {
            /*
             * Keep the operation retryable.
             * Payment has already succeeded; do not charge again here.
             */
            String message = exceptionMessages(ex).toUpperCase();

            if (message.contains("FLIGHT_OFFER_UNAVAILABLE")
                    || message.contains("OFFER_NO_LONGER_AVAILABLE")
                    || message.contains("FLIGHT_PRICE_CHANGED")) {
                booking.setStatus(BookingStatus.FAILED);
            } else {
                booking.setStatus(BookingStatus.PAYMENT_PROCESSING);
            }

            bookingRepository.saveAndFlush(booking);
            throw ex;
        }
    }

    private String exceptionMessages(Throwable error) {
        StringBuilder messages = new StringBuilder();

        Throwable current = error;

        while (current != null) {
            if (current.getMessage() != null) {
                messages.append(' ').append(current.getMessage());
            }

            current = current.getCause();
        }

        return messages.toString();
    }

    // 5. GET BOOKING DETAILS

    @Transactional(readOnly = true)
    public BookingDetails getBookingDetails(String bookingReference) {

        Booking booking = bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found: " + bookingReference));

        return buildBookingDetails(booking);
    }

    // 6. GET PAYMENT DETAILS

    @Transactional(readOnly = true)
    public PaymentDetails getPaymentDetails(
            String bookingReference) {

        Booking booking = bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found: " + bookingReference));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidBookingStateException(
                    "Booking is not awaiting payment. Current status: "
                            + booking.getStatus());
        }

        if (booking.getPaymentAmount() == null
                || booking.getPaymentAmount().signum() <= 0) {
            throw new IllegalStateException(
                    "Valid payment amount is not available for booking: "
                            + bookingReference);
        }

        if (booking.getPaymentCurrency() == null
                || booking.getPaymentCurrency().isBlank()) {
            throw new IllegalStateException(
                    "Payment currency is not available for booking: "
                            + bookingReference);
        }

        /*
         * Validate the latest flight offer before Payment Service creates
         * a Stripe PaymentIntent.
         */
        Map<String, Object> latestOffer =
                flightServiceClient.repriceOffer(booking.getOfferId());

        if (latestOffer == null
                || latestOffer.get("price") == null
                || latestOffer.get("currency") == null) {
            throw new IllegalStateException(
                    "FLIGHT_OFFER_UNAVAILABLE: Unable to verify current offer");
        }

        BigDecimal latestPrice =
                new BigDecimal(
                        String.valueOf(latestOffer.get("price")));

        String latestCurrency =
                String.valueOf(latestOffer.get("currency"));

        boolean priceChanged =
                booking.getOriginalAmount() == null
                        || booking.getOriginalAmount()
                                .compareTo(latestPrice) != 0;

        boolean currencyChanged =
                booking.getOriginalCurrency() == null
                        || !booking.getOriginalCurrency()
                                .equalsIgnoreCase(latestCurrency);

        if (priceChanged || currencyChanged) {
            throw new IllegalStateException(
                    "FLIGHT_PRICE_CHANGED: The flight price or currency "
                            + "has changed. Create a new booking with the "
                            + "latest offer before payment.");
        }

        return new PaymentDetails(
                booking.getBookingReference(),
                booking.getPaymentAmount(),
                booking.getPaymentCurrency(),
                booking.getExchangeRate(),
                booking.getStatus().name());
    }

    // 7. USER BOOKINGS

    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    public BookingDetails getMyBookingDetails(
            Long userId,
            String bookingReference) {

        Booking booking = bookingRepository
                .findByBookingReferenceAndUserId(
                        bookingReference,
                        userId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found"));

        return buildBookingDetails(booking);
    }

    // 8. CANCEL BOOKING

    @Transactional
    public CancelBookingResponse cancelBooking(
            Long userId,
            String bookingReference,
            CancelBookingRequest request) {

        Booking booking = bookingRepository
                .findByBookingReferenceAndUserId(
                        bookingReference,
                        userId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new CancelBookingResponse(
                    bookingReference,
                    "CANCELLED",
                    "Booking is already cancelled");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Only confirmed bookings can be cancelled");
        }

        /*
         * Actual airline cancellation and Stripe refund must be handled
         * through the respective services.
         */
        booking.setStatus(BookingStatus.CANCEL_REQUESTED);
        bookingRepository.save(booking);

        return new CancelBookingResponse(
                bookingReference,
                "CANCEL_REQUESTED",
                "Cancellation request accepted");
    }

    // 9. BUILD BOOKING DETAILS

    private BookingDetails buildBookingDetails(Booking booking) {

        List<Passenger> passengers =
                passengerRepository.findByBookingId(booking.getId());

        List<PassengerDetails> passengerDetails =
                passengers.stream()
                        .map(passenger -> new PassengerDetails(
                                passenger.getFirstName(),
                                passenger.getLastName(),
                                passenger.getDateOfBirth(),
                                passenger.getGender(),
                                passenger.getNationality(),
                                passenger.getEmail(),
                                passenger.getPhone(),
                                passenger.getPassportNumber()))
                        .toList();

        return new BookingDetails(
                booking.getBookingReference(),
                booking.getUserId(),
                booking.getOfferId(),
                booking.getAirline(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getDepartureTime(),
                booking.getArrivalTime(),
                booking.getTotalAmount(),
                booking.getCurrency(),
                booking.getPaymentAmount(),
                booking.getPaymentCurrency(),
                booking.getExchangeRate(),
                booking.getStatus().name(),
                booking.getDuffelOrderId(),
                booking.getAirlineBookingReference(),
                booking.getCreatedAt(),
                booking.getFlightNumber(),
                passengerDetails);
    }

    // 10. GET PASSENGERS

    @Transactional(readOnly = true)
    public List<PassengerDetails> getPassengers(
            Long userId,
            String bookingReference) {

        Booking booking = bookingRepository
                .findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found: " + bookingReference));

        if (!booking.getUserId().equals(userId)) {
            throw new BookingOwnershipException(
                    "You are not allowed to access this booking");
        }

        return passengerRepository
                .findByBookingId(booking.getId())
                .stream()
                .map(passenger -> new PassengerDetails(
                        passenger.getFirstName(),
                        passenger.getLastName(),
                        passenger.getDateOfBirth(),
                        passenger.getGender(),
                        passenger.getNationality(),
                        passenger.getEmail(),
                        passenger.getPhone(),
                        passenger.getPassportNumber()))
                .toList();
    }

    // 11. MAP BOOKING RESPONSE

    private BookingResponse mapToBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingReference(),
                booking.getOfferId(),
                booking.getUserId(),
                booking.getTotalAmount(),
                booking.getCurrency(),
                booking.getPaymentAmount(),
                booking.getPaymentCurrency(),
                booking.getExchangeRate(),
                booking.getStatus());
    }
}
