package com.flightbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightbooking.dto.CreatePaymentRequest;
import com.flightbooking.dto.PaymentResponse;
import com.flightbooking.dto.PaymentStatusResponse;
import com.flightbooking.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
    		@Valid
            @RequestBody CreatePaymentRequest request) throws Exception{

        return ResponseEntity.ok(
        			paymentService.createPayment(request)
        );
    }

    @GetMapping("/{bookingReference}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String bookingReference) {

        return ResponseEntity.ok(
                paymentService.getPaymentStatus(bookingReference)
        );
    }
}
