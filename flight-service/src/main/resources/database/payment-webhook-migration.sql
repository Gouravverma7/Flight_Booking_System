
USE flight_payment_db;

ALTER TABLE stripe_webhook_events
    MODIFY COLUMN payload LONGTEXT NOT NULL;

-- Verify the actual schema.
SHOW COLUMNS FROM stripe_webhook_events LIKE 'payload';

-- Inspect saved webhook events.
SELECT
    stripe_event_id,
    event_type,
    status,
    attempts,
    last_error,
    created_at,
    updated_at
FROM stripe_webhook_events
ORDER BY created_at DESC;
