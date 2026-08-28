# Enterprise Billing & Payment Processing Platform

## Overview

Enterprise Billing & Payment Processing Platform is a backend application designed to model real-world billing and payment workflows.

The project is being developed using Java and Spring Boot with the objective of implementing enterprise backend engineering concepts such as REST APIs, database persistence, asynchronous messaging, caching, security, testing, containerization and microservices.

## Business Domain

The system will manage the lifecycle of billing and payment transactions including:

- Customer management
- Account management
- Bill generation
- Invoice management
- Payment processing
- Payment allocation
- Financial transaction tracking
- Reconciliation
- Notifications

## High-Level Business Flow

Customer → Account → Bill → Invoice → Payment → Payment Allocation → Financial Transaction → Reconciliation → Notification

## Architecture

The application will initially be developed as a modular monolith.

Business capabilities such as customer management, account management, billing, payment processing, transaction management, reconciliation and notifications will be organized as separate modules within a single Spring Boot application.

As the project evolves, selected modules may be extracted into independently deployable microservices based on clear business boundaries and integration requirements.

## Payment Processing and Razorpay Integration

The platform supports bill-aware payment processing with Razorpay integration.

Current capabilities include:

- Razorpay Test Mode payment order creation
- Dynamic Razorpay Checkout integration
- Payment signature verification
- Razorpay `payment.captured` webhook handling
- Idempotent payment completion
- Bill-aware payment creation
- Outstanding bill amount validation before gateway order creation
- Partial bill payments
- Automatic payment allocation after successful payment
- Automatic bill transition from `ISSUED` to `PAID`
- Payable bill retrieval based on remaining balance
- Prevention of overpayment
- Financial transaction creation
- Payment reconciliation
- Asynchronous payment-success notifications
- SMTP email delivery
- Automatic notification retry handling
- PostgreSQL persistence using Flyway migrations
- Cloud deployment using Render and Neon PostgreSQL

### Payment Flow

```text
Customer selects Account
        ↓
Fetch payable bills
        ↓
Select Bill
        ↓
Determine remaining bill amount
        ↓
Create PENDING Payment
        ↓
Create Razorpay Order
        ↓
Complete Razorpay Checkout
        ↓
Verify payment / process webhook
        ↓
Payment becomes SUCCESS
        ↓
Create Payment Allocation
        ↓
Create Financial Transactions
        ↓
Create PAYMENT_SUCCESS Notification
        ↓
Publish Notification Event to Kafka
        ↓
Kafka Consumer Processes Notification
        ↓
Send Email through SMTP
        ↓
Notification becomes SENT
        ↓
Recalculate Bill Balance
        ↓
Fully allocated?
   ┌────┴────┐
   No        Yes
   ↓          ↓
ISSUED       PAID
```

## Notification Processing

Payment-success notifications are processed asynchronously using Apache Kafka.

```text
Payment SUCCESS
        ↓
Create Notification
status = PENDING
        ↓
Publish NotificationEvent
        ↓
Kafka Topic
payment-notification
        ↓
Notification Consumer
        ↓
Load Notification from Database
        ↓
Send Email using SMTP
        ↓
   ┌────┴────┐
 Success    Failure
   ↓          ↓
 SENT       FAILED
```

## Notification Retry

Failed notifications and stale `PENDING` notifications are automatically handled by a scheduled retry process.

```text
FAILED Notification
        or
Stale PENDING Notification
        ↓
Notification Retry Scheduler
        ↓
retry_count < maximum?
        ↓
Retry Email Delivery
   ┌────┴────┐
 Success    Failure
   ↓          ↓
 SENT       FAILED
```

The retry mechanism tracks:

- Retry count
- Last delivery attempt timestamp
- Latest failure reason
- Successful send timestamp

The initial Kafka delivery attempt does not count as a retry.

The current maximum retry count is 3.

The retry scheduler runs every 60 seconds by default and can be configured using the `NOTIFICATION_RETRY_DELAY_MS` environment variable.

## Reconciliation

Successful gateway payments can be reconciled against Razorpay payment information.

The reconciliation process validates:

- Internal payment amount against gateway amount
- Internal payment status against gateway status
- Internal gateway order ID against Razorpay order ID
- Missing gateway payments

Supported reconciliation statuses include:

- `MATCHED`
- `AMOUNT_MISMATCH`
- `STATUS_MISMATCH`
- `ORDER_MISMATCH`
- `PAYMENT_NOT_FOUND`

## Testing

The project uses JUnit and Mockito for automated testing.

Current automated test coverage includes:

- Payment reconciliation service behavior
- Successful notification delivery
- SMTP failure handling
- Already-sent notification protection
- Successful retry of failed notifications
- Recovery of stale `PENDING` notifications
- Skipping recent `PENDING` notifications
- Notification retry failure handling
- Spring Boot application context validation

## Database Migrations

Database schema changes are managed using Flyway.

Notification-related migrations:

- `V10__create_notifications.sql`
- `V11__add_notification_retry_fields.sql`

## Local Infrastructure

Local development uses Docker Compose.

The current local environment includes:

- PostgreSQL
- Apache Kafka
- ZooKeeper

## API Documentation

Swagger/OpenAPI is available locally at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Documentation

- [Payment Flow](docs/payment-flow.md)
- [Notification and Email Processing Flow](docs/notification-flow.md)

## Project Status

In Development