ALTER TABLE notifications
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE notifications
    ADD COLUMN last_attempt_at TIMESTAMP;

ALTER TABLE notifications
    ADD COLUMN failure_reason VARCHAR(500);