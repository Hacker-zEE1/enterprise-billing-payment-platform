# Enterprise Billing & Payment Processing Platform

## Overview

Enterprise Billing & Payment Processing Platform is a backend application designed to model real-world billing and payment workflows.

The project is being developed using Java and Spring Boot with the objective of implementing enterprise backend engineering concepts such as REST APIs, database persistence, asynchronous messaging, caching, security, testing, containerization and microservices.

## Business Domain

The system manages the lifecycle of billing and payment transactions including:

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

The application is currently developed as a modular monolith.

Business capabilities such as customer management, account management, billing, payment processing, transaction management, reconciliation and notifications are organized as separate modules within a single Spring Boot application.

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
- Profile-based email delivery using SMTP locally and Resend HTTP API in cloud
- Automatic notification retry handling
- PostgreSQL persistence using Flyway migrations
- Cloud deployment using Render and Neon PostgreSQL
- Managed Kafka integration using Aiven

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
Send Email through EmailSender
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
Send Email through EmailSender
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

### Cloud Email Delivery

Email delivery uses the `EmailSender` abstraction.

- Local: Gmail SMTP through `SmtpEmailSender`
- Cloud: Resend HTTP API through `ResendEmailSender`
- Kafka: Aiven managed Kafka
- Retry: scheduled retry for failed/stale notifications
- Maximum retries: 3

The cloud environment uses the Spring `cloud` profile so the HTTP-based Resend sender is selected instead of SMTP.

The Resend REST API is called using Spring `RestClient` to avoid an OkHttp dependency conflict between the Resend Java SDK and the Razorpay Java SDK.

See [Notification and Email Processing Flow](docs/notification-flow.md).

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
- Email delivery failure handling
- Already-sent notification protection
- Successful retry of failed notifications
- Recovery of stale `PENDING` notifications
- Skipping recent `PENDING` notifications
- Notification retry failure handling
- JWT authentication filter behavior
- Customer authorization
- Account authorization
- Bill authorization
- Payment authorization
- Payment allocation authorization
- Reconciliation authorization
- Razorpay webhook security
- Registration token validation
- Registration token expiry and one-time usage
- ADMIN-only registration invitation creation
- Application user registration
- Spring Boot application context validation

Current test suite:

```text
Tests run: 61
Failures: 0
Errors: 0
Skipped: 0
```
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

## Security

The platform uses Spring Security with stateless JWT authentication and role-based authorization.

Implemented security features include:

- JWT-based authentication
- BCrypt password hashing
- `ADMIN` and `CUSTOMER` roles
- Method-level authorization with `@PreAuthorize`
- Customer and account ownership checks
- One-time registration invitations
- Hashed and expiring registration tokens
- Pessimistic locking to prevent concurrent token reuse
- Customer lifecycle synchronization with login access
- Immutable login email through normal profile updates
- Proper `401 Unauthorized` and `403 Forbidden` handling
- Razorpay webhook signature validation
- Swagger/OpenAPI JWT Bearer authentication support

Security-related migrations:

- `V12__create_app_users.sql` - Application users
- `V13__link_app_user_to_customer.sql` - Customer/user relationship
- `V14__create_registration_tokens.sql` - Registration tokens

## Documentation

- [Payment Flow](docs/payment-flow.md)
- [Notification and Email Processing Flow](docs/notification-flow.md)
- [Security Architecture](docs/SECURITY.md)

## Project Status

In Development