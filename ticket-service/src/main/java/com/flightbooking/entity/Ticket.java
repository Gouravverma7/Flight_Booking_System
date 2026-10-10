package com.flightbooking.entity;

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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "tickets",
		indexes = {
				@Index(
						name = "idx_ticket_booking",
						columnList = "booking_reference",
						unique = true
						)
		}
)
@Data
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(
            name = "booking_reference",
            nullable = false,
            unique = true
    )
    private String bookingReference;

    @Column(
            name = "duffel_order_id",
            nullable = false
    )
    private String duffelOrderId;

	@Column(name = "airline_booking_reference")
	private String airlineBookingReference;

	@Column(name = "pdf_file_name",
			nullable = false)
	private String pdfFileName;

	@Column(name = "pdf_file_path",
			nullable = false)
	private String pdfFilePath;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void onCreate() {
		createdAt = LocalDateTime.now();
	}



































}
