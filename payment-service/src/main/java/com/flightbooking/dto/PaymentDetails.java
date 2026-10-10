package com.flightbooking.dto;

import java.math.BigDecimal;

public record PaymentDetails(
		String bookingReference,
        BigDecimal paymentAmount,
        String paymentCurrency,
        BigDecimal exchangeRate,
        String status) {

}
