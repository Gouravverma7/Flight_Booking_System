package com.flightbooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flightbooking.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>{
	Optional<Ticket> findByBookingReference(String bookingReference);
}
