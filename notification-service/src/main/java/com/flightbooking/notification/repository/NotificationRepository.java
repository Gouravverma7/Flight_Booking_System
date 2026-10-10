package com.flightbooking.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightbooking.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>{

	Optional<Notification> findByBookingReferenceAndNotificationType(
			String bookingReference, String notificationType);
}
