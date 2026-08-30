# Notification and Email Processing Flow

## Overview

The notification module sends payment-success email notifications asynchronously after a payment is completed successfully.

The flow uses:

- PostgreSQL for notification persistence
- Apache Kafka for asynchronous message delivery
- Spring Kafka producer and consumer
- `EmailSender` abstraction for email delivery
- Gmail SMTP for local email delivery
- Resend HTTP API for cloud email delivery
- Scheduled retry processing for failed or stale notifications
- JUnit and Mockito for unit testing

## Notification Flow

When a payment becomes successful:

1. Payment processing completes successfully.
2. A `PAYMENT_SUCCESS` notification is created in the `notifications` table.
3. The notification is initially stored with status `PENDING`.
4. A `NotificationEvent` containing the notification ID is published to Kafka.
5. The Kafka consumer receives the event.
6. The consumer loads the notification from PostgreSQL.
7. The email is sent using the active `EmailSender` implementation.
8. On success:
   - notification status becomes `SENT`
   - `sent_at` is populated
   - `last_attempt_at` is populated
9. On failure:
   - notification status becomes `FAILED`
   - `last_attempt_at` is populated
   - `failure_reason` is stored

## Kafka Topic

Topic:

```text
payment-notification