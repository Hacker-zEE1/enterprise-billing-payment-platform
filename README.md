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
Recalculate bill balance
        ↓
Fully allocated?
   ┌────┴────┐
   No        Yes
   ↓          ↓
ISSUED       PAID

## Project Status

In Development