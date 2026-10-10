package com.flightbooking.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flightbooking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	Optional<Booking> findByBookingReference(String bookingRefrence);

	List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Booking> findByBookingReferenceAndUserId(
	        String bookingReference,
	        Long userId
	);
}
