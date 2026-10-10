package com.flightbooking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bookings",
			indexes = {
					@Index(name = "idx_booking_user_id",
						   columnList = "user_id"
					),
					@Index(name = "idx_booking_reference",
							columnList = "booking_reference"
					)
			}, uniqueConstraints = {
					@UniqueConstraint(name = "uk_booking_reference",
							columnNames = "booking_reference"
							)
					}
)
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "booking_reference", nullable = false, unique = true, length = 20)
	private String bookingReference;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String offerId;

	private String airline;
	private String origin;
	private String destination;

	private String departureTime;
	private String arrivalTime;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false, length = 3)
	private String currency;

	private BigDecimal originalAmount;
	private String originalCurrency;

	private BigDecimal paymentAmount;
	private String paymentCurrency;

	private BigDecimal exchangeRate;

	private String duffelOrderId;
	private String airlineBookingReference;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status;

	@Column(name = "flight_number")
	private String flightNumber;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {

		LocalDateTime now = LocalDateTime.now();

		createdAt = now;
		updatedAt = now;

		if (status == null) {
			status = BookingStatus.PENDING_PAYMENT;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

}
