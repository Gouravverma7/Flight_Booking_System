package com.flightbooking.dto;

import java.math.BigDecimal;

import com.flightbooking.entity.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponse {

	private String bookingReference;
	private String paymentIntentId;
	private String clientSecret;
	private BigDecimal amount;
	private String currency;
	private PaymentStatus status;

	public PaymentResponse(
			String bookingReference,
            String paymentIntentId,
            String clientSecret,
            BigDecimal amount,
            String currency,
            PaymentStatus status
    ) {
		this.bookingReference = bookingReference;
        this.paymentIntentId = paymentIntentId;
        this.clientSecret = clientSecret;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

}
