package com.flightbooking.service;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import com.flightbooking.client.BookingServiceClient;
import com.flightbooking.client.NotificationServiceClient;
import com.flightbooking.dto.CreatePaymentRequest;
import com.flightbooking.dto.PaymentDetails;
import com.flightbooking.dto.PaymentResponse;
import com.flightbooking.dto.PaymentStatusResponse;
import com.flightbooking.entity.Payment;
import com.flightbooking.entity.PaymentStatus;
import com.flightbooking.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.net.RequestOptions;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final BookingServiceClient bookingServiceClient;
	private final NotificationServiceClient notificationServiceClient;

	public PaymentService(PaymentRepository paymentRepository, BookingServiceClient bookingServiceClient,
			NotificationServiceClient notificationServiceClient) {

		this.paymentRepository = paymentRepository;
		this.bookingServiceClient = bookingServiceClient;
		this.notificationServiceClient = notificationServiceClient;
	}

	public PaymentResponse createPayment(CreatePaymentRequest request)
	        throws StripeException {

	    String bookingReference = request.getBookingReference();

	    // 1. Check whether payment already exists
	    var existingPayment =
	            paymentRepository.findByBookingReference(bookingReference);

	    if (existingPayment.isPresent()) {
	        Payment payment = existingPayment.get();

	        if (payment.getStripePaymentIntentId() == null
	                || payment.getStripePaymentIntentId().isBlank()) {
	            throw new IllegalStateException(
	                    "Payment exists but Stripe PaymentIntent ID is missing for booking: "
	                            + bookingReference
	            );
	        }

	        // Retrieve the existing Stripe PaymentIntent
	        PaymentIntent existingIntent =
	                PaymentIntent.retrieve(payment.getStripePaymentIntentId());

	        // Never restart checkout for a successful payment
	        if ("succeeded".equals(existingIntent.getStatus())) {
	            return new PaymentResponse(
	                    payment.getBookingReference(),
	                    payment.getStripePaymentIntentId(),
	                    null,
	                    payment.getAmount(),
	                    payment.getCurrency(),
	                    payment.getStatus()
	            );
	        }

	        // Return client secret for an incomplete payment
	        return new PaymentResponse(
	                payment.getBookingReference(),
	                payment.getStripePaymentIntentId(),
	                existingIntent.getClientSecret(),
	                payment.getAmount(),
	                payment.getCurrency(),
	                payment.getStatus()
	        );
	    }

	    // 2. Fetch authoritative payment details from Booking Service
	    PaymentDetails bookingPayment =
	            bookingServiceClient.getPaymentDetails(bookingReference);

	    if (bookingPayment == null) {
	        throw new IllegalStateException(
	                "Payment details not found for booking: " + bookingReference
	        );
	    }

	    // 3. Booking must be awaiting payment
	    if (!"PENDING_PAYMENT".equals(bookingPayment.status())) {
	        throw new IllegalStateException(
	                "Booking is not awaiting payment. Current status: "
	                        + bookingPayment.status()
	        );
	    }

	    // 4. Validate amount
	    var amount = bookingPayment.paymentAmount();

	    if (amount == null || amount.signum() <= 0) {
	        throw new IllegalStateException(
	                "Invalid payment amount for booking: " + bookingReference
	        );
	    }

	    // 5. Validate currency
	    String currency = bookingPayment.paymentCurrency();

	    if (currency == null || currency.isBlank()) {
	        throw new IllegalStateException(
	                "Payment currency is missing for booking: " + bookingReference
	        );
	    }

	    currency = currency.trim().toUpperCase();

	    // 6. Convert to Stripe minor units
	    long amountInMinorUnit = amount
	            .movePointRight(2)
	            .longValueExact();

	    // 7. Create Stripe PaymentIntent with an idempotency key
	    PaymentIntentCreateParams params =
	            PaymentIntentCreateParams.builder()
	                    .setAmount(amountInMinorUnit)
	                    .setCurrency(currency.toLowerCase())
	                    .putMetadata("booking_reference", bookingReference)
	                    .build();

	    RequestOptions requestOptions = RequestOptions.builder()
	            .setIdempotencyKey("booking-payment-" + bookingReference)
	            .build();

	    PaymentIntent paymentIntent =
	            PaymentIntent.create(params, requestOptions);

	    // 8. Save payment in database
	    Payment payment = new Payment();

	    payment.setBookingReference(bookingReference);
	    payment.setStripePaymentIntentId(paymentIntent.getId());
	    payment.setAmount(amount);
	    payment.setCurrency(currency);
	    payment.setStatus(PaymentStatus.CREATED);

	    try {
	        payment = paymentRepository.saveAndFlush(payment);
	    } catch (DataIntegrityViolationException ex) {
	        var concurrentPayment =
	                paymentRepository.findByBookingReference(bookingReference);

	        if (concurrentPayment.isPresent()
	                && paymentIntent.getId().equals(
	                        concurrentPayment.get().getStripePaymentIntentId())) {
	            Payment savedPayment = concurrentPayment.get();
	            PaymentIntent savedIntent =
	                    PaymentIntent.retrieve(savedPayment.getStripePaymentIntentId());

	            String clientSecret = "succeeded".equals(savedIntent.getStatus())
	                    ? null
	                    : savedIntent.getClientSecret();

	            return new PaymentResponse(
	                    savedPayment.getBookingReference(),
	                    savedPayment.getStripePaymentIntentId(),
	                    clientSecret,
	                    savedPayment.getAmount(),
	                    savedPayment.getCurrency(),
	                    savedPayment.getStatus()
	            );
	        }

	        // The constraint violation was not the expected concurrent request.
	        throw ex;
	    }

	    // 9. Return the client secret to the frontend
	    return new PaymentResponse(
	            payment.getBookingReference(),
	            payment.getStripePaymentIntentId(),
	            paymentIntent.getClientSecret(),
	            payment.getAmount(),
	            payment.getCurrency(),
	            payment.getStatus()
	    );
	}

	@Transactional(readOnly = true)
	public PaymentStatusResponse getPaymentStatus(String bookingReference) {

		var payment = paymentRepository.findByBookingReference(bookingReference)
				.orElseThrow(() -> new IllegalArgumentException("Payment not found for booking: " + bookingReference));

		return new PaymentStatusResponse(payment.getBookingReference(), payment.getAmount(), payment.getCurrency(),
				payment.getStatus(), payment.getCreatedAt(), payment.getUpdatedAt());
	}

}
