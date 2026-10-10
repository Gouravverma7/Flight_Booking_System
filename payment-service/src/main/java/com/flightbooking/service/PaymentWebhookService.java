package com.flightbooking.service;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbooking.client.BookingServiceClient;
import com.flightbooking.client.NotificationServiceClient;
import com.flightbooking.entity.Payment;
import com.flightbooking.entity.PaymentStatus;
import com.flightbooking.entity.StripeWebhookEvent;
import com.flightbooking.entity.StripeWebhookEventStatus;
import com.flightbooking.repository.PaymentRepository;
import com.flightbooking.repository.StripeWebhookEventRepository;

@Service
public class PaymentWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentWebhookService.class);

    private static final int MAX_ATTEMPTS = 10;

    private final PaymentRepository paymentRepository;
    private final StripeWebhookEventRepository eventRepository;
    private final BookingServiceClient bookingServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ObjectMapper objectMapper;

    public PaymentWebhookService(
            PaymentRepository paymentRepository,
            StripeWebhookEventRepository eventRepository,
            BookingServiceClient bookingServiceClient,
            NotificationServiceClient notificationServiceClient,
            ObjectMapper objectMapper) {

        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.bookingServiceClient = bookingServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.objectMapper = objectMapper;
    }


    public void enqueueEvent(
            com.stripe.model.Event event,
            String payload) {

        if (event.getId() == null || event.getId().isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe event ID is missing");
        }

        if (eventRepository.findByStripeEventId(event.getId()).isPresent()) {
            // Stripe may deliver an event more than once.
            return;
        }

        StripeWebhookEvent queued = new StripeWebhookEvent();
        queued.setStripeEventId(event.getId());
        queued.setEventType(event.getType());
        queued.setPayload(payload);
        queued.setStatus(StripeWebhookEventStatus.PENDING);
        queued.setAttempts(0);

        try {
            eventRepository.saveAndFlush(queued);
        } catch (DataIntegrityViolationException ex) {
            // A concurrent request may already have inserted this event.
            if (eventRepository.findByStripeEventId(event.getId()).isEmpty()) {
                throw ex;
            }
        }
    }


    @Scheduled(fixedDelayString =
            "${stripe.webhook-worker-delay-ms:1000}")
    public void processPendingEvents() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusMinutes(2);

        // Recover events abandoned during a service restart.
        List<StripeWebhookEvent> staleEvents =
                eventRepository.findByStatusAndUpdatedAtBefore(
                        StripeWebhookEventStatus.PROCESSING,
                        cutoff);

        for (StripeWebhookEvent stale : staleEvents) {
            stale.setStatus(StripeWebhookEventStatus.PENDING);
            eventRepository.save(stale);
        }

        List<StripeWebhookEvent> pendingEvents =
                eventRepository.findTop10ByStatusOrderByCreatedAtAsc(
                        StripeWebhookEventStatus.PENDING);

        for (StripeWebhookEvent queued : pendingEvents) {
            processOne(queued);
        }
    }

    private void processOne(StripeWebhookEvent queued) {

        queued.setStatus(StripeWebhookEventStatus.PROCESSING);
        queued.setAttempts(queued.getAttempts() + 1);
        queued.setLastError(null);
        eventRepository.saveAndFlush(queued);

        try {
            JsonNode root =
                    objectMapper.readTree(queued.getPayload());

            JsonNode object =
                    root.path("data").path("object");

            switch (queued.getEventType()) {
                case "payment_intent.succeeded" -> {
                    String intentId = object.path("id").asText(null);

                    if (intentId == null || intentId.isBlank()) {
                        throw new IllegalStateException(
                                "PaymentIntent ID missing from event");
                    }

                    long amountMinor = object.path("amount_received")
                            .asLong(object.path("amount").asLong(-1));

                    String currency =
                            object.path("currency").asText(null);

                    markSucceeded(intentId, amountMinor, currency);
                }

                case "payment_intent.payment_failed" -> {
                    String intentId = object.path("id").asText(null);

                    if (intentId == null || intentId.isBlank()) {
                        throw new IllegalStateException(
                                "PaymentIntent ID missing from event");
                    }

                    markFailed(intentId);
                }

                default -> log.info(
                        "Ignoring unsupported Stripe event type: {}",
                        queued.getEventType());
            }

            queued.setStatus(StripeWebhookEventStatus.COMPLETED);
            queued.setLastError(null);
            eventRepository.save(queued);

        } catch (Exception ex) {
            StripeWebhookEventStatus nextStatus =
                    queued.getAttempts() >= MAX_ATTEMPTS
                            ? StripeWebhookEventStatus.DEAD_LETTER
                            : StripeWebhookEventStatus.PENDING;

            queued.setStatus(nextStatus);
            queued.setLastError(truncate(
                    ex.getMessage(), 4000));

            eventRepository.save(queued);

            log.error(
                    "Stripe event processing failed. eventId={}, attempt={}, status={}",
                    queued.getStripeEventId(),
                    queued.getAttempts(),
                    nextStatus,
                    ex);
        }
    }

    private void markSucceeded(
            String paymentIntentId,
            long receivedAmountMinor,
            String receivedCurrency) {

        Payment payment = paymentRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException(
                        "Payment not found for PaymentIntent: "
                                + paymentIntentId));

        if (payment.getAmount() == null
                || payment.getCurrency() == null) {
            throw new IllegalStateException(
                    "Stored payment amount/currency is missing");
        }

        // Stripe amount is in the currency's smallest unit.
        // This conversion assumes a two-decimal currency, such as INR/USD.
        long expectedAmountMinor = payment.getAmount()
                .movePointRight(2)
                .longValueExact();

        if (receivedAmountMinor != expectedAmountMinor) {
            throw new IllegalStateException(
                    "Stripe amount mismatch. Expected "
                            + expectedAmountMinor
                            + " but received " + receivedAmountMinor);
        }

        if (receivedCurrency == null
                || !payment.getCurrency()
                        .equalsIgnoreCase(receivedCurrency)) {
            throw new IllegalStateException(
                    "Stripe currency mismatch. Expected "
                            + payment.getCurrency()
                            + " but received " + receivedCurrency);
        }

        String bookingReference = payment.getBookingReference();

        // Do not skip booking confirmation merely because payment is already
        // SUCCEEDED. The previous attempt may have failed at Booking Service.
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.saveAndFlush(payment);
        }


     // Booking confirmation failure must remain retryable.
        bookingServiceClient.confirmPayment(
                bookingReference,
                paymentIntentId
        );

        // Notification failure must not retry the entire payment workflow.
        try {
            notificationServiceClient.sendBookingConfirmation(
                    bookingReference
            );
        } catch (Exception ex) {
            log.error(
                    "Booking confirmation notification failed. "
                            + "bookingReference={}",
                    bookingReference,
                    ex
            );
        }
    }

    private void markFailed(String paymentIntentId) {

        paymentRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(payment -> {
                    // A late failure event must not downgrade a successful payment.
                    if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                    }
                });
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "Unknown processing error";
        }

        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength);
    }
}

