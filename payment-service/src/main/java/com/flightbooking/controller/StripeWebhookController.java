package com.flightbooking.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flightbooking.service.PaymentWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    private final PaymentWebhookService webhookService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(
            PaymentWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(
                    value = "Stripe-Signature",
                    required = false) String signature) {

        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Missing Stripe-Signature header");
        }

        final Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    signature,
                    webhookSecret);
        } catch (SignatureVerificationException ex) {
            return ResponseEntity.badRequest()
                    .body("Invalid Stripe signature");
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body("Invalid Stripe webhook payload");
        }

        try {
            // Return success only after the event is durably saved.
            webhookService.enqueueEvent(event, payload);
            return ResponseEntity.ok("Webhook received");

        } catch (Exception ex) {
            // Stripe can retry when database persistence fails.
            return ResponseEntity.internalServerError()
                    .body("Webhook could not be saved");
        }
    }
}
