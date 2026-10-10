package com.flightbooking.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flightbooking.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
	List<Passenger> findByBookingId(Long bookingId);
}
