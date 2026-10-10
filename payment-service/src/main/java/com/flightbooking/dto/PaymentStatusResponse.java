package com.flightbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.flightbooking.entity.PaymentStatus;

public record PaymentStatusResponse(
        String bookingReference,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}