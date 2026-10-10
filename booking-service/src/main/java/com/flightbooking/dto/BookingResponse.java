package com.flightbooking.dto;

import java.math.BigDecimal;

import com.flightbooking.entity.BookingStatus;

import lombok.Data;

@Data
public class BookingResponse {
	private String bookingReference;
	private String offerId;
	private Long userId;
	private BigDecimal amount;
	private String currency;

	// Customer payment price
    private BigDecimal paymentAmount;
    private String paymentCurrency;

    private BigDecimal exchangeRate;

	private BookingStatus status;

	public BookingResponse(String bookingReference,
							String offerId,
							Long userId,
							BigDecimal amount,
							String currency,
							BigDecimal paymentAmount,
				            String paymentCurrency,
				            BigDecimal exchangeRate,
							BookingStatus status) {
		super();
		this.bookingReference = bookingReference;
		this.offerId = offerId;
		this.userId = userId;
		this.amount = amount;
		this.currency = currency;
		 this.paymentAmount = paymentAmount;
	        this.paymentCurrency = paymentCurrency;
	        this.exchangeRate = exchangeRate;
		this.status = status;
	}


}
