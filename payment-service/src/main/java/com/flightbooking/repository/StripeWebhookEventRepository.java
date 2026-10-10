package com.flightbooking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightbooking.entity.StripeWebhookEvent;
import com.flightbooking.entity.StripeWebhookEventStatus;

public interface StripeWebhookEventRepository
        extends JpaRepository<StripeWebhookEvent, Long> {

    Optional<StripeWebhookEvent> findByStripeEventId(
            String stripeEventId);

    List<StripeWebhookEvent>
        findTop10ByStatusOrderByCreatedAtAsc(
            StripeWebhookEventStatus status);

    List<StripeWebhookEvent>
        findByStatusAndUpdatedAtBefore(
            StripeWebhookEventStatus status,
            LocalDateTime cutoff);
}
