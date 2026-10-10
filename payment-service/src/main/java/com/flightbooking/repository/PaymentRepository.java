package com.flightbooking.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flightbooking.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	 Optional<Payment> findByBookingReference(String bookingReference);

	 Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
}
