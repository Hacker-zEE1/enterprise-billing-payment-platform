# Payment Processing and Bill Allocation

## Overview

The payment module supports bill-aware payment processing using Razorpay.

Payments are created against an account and linked to the bill they are intended to settle. After successful Razorpay processing, the payment is automatically allocated to the associated bill.

The design supports both partial and full bill payments.

---

## Main Components

### Payment

Represents money received or being processed for an account.

Important statuses:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `CANCELLED`

A gateway payment starts as `PENDING` and becomes `SUCCESS` only after verified Razorpay completion.

---

### Bill

Represents an amount payable by an account.

Relevant statuses:

- `DRAFT`
- `ISSUED`
- `PAID`
- `CANCELLED`

Only `ISSUED` bills can receive new payments.

---

### Payment Allocation

Represents the amount of a successful payment applied to a bill.

This allows a bill to be settled through multiple payments.

Example:

```text
Bill Amount: ₹300

Payment 1: ₹30
Payment 2: ₹40
Payment 3: ₹230

Total Allocated: ₹300
Remaining: ₹0
Bill Status: PAID
```

---

## Automatic vs Manual Payment Allocation

The platform supports both automatic and manual payment allocation.

### Automatic Allocation

Razorpay bill payments are automatically allocated because the customer selects the bill before initiating the payment.

```text
Customer selects Bill
        ↓
Create Razorpay Payment
        ↓
Payment SUCCESS
        ↓
Automatic PaymentAllocation
        ↓
Bill balance updated
```

In this flow, the payment already knows which bill it is intended to settle.

The customer or operator does not need to manually create an allocation after a successful Razorpay payment.

### Manual Allocation
> Note: The current Razorpay flow is bill-directed and automatically allocated. Manual allocation is intended for future account-level payments that are created without an initial bill association.
The Payment Allocation API is retained to support account-level payments that are not initially tied to a specific bill.

Example:

```text
Account receives ₹1,000
        ↓
Payment recorded
        ↓
Operator allocates later

₹600 → Bill A
₹400 → Bill B
```

This pattern is useful for scenarios such as:

- Bank transfer payments
- Imported payment files
- Payments received without a bill reference
- Enterprise back-office allocation
- Future rule-based payment matching

### Allocation Strategy

```text
Bill-directed gateway payment
        ↓
Automatic Allocation

Account-level payment
        ↓
Manual / Rule-based Allocation
```

The `PaymentAllocationController` therefore remains part of the application even though Razorpay bill payments use automatic allocation.

This separation allows the platform to support both customer-facing bill payment and enterprise-style account-level payment processing.

---
## Financial Transactions

The platform records immutable financial transaction entries for successful payment activity.

Current transaction types:

- `PAYMENT_RECEIVED`
- `PAYMENT_ALLOCATED`

A successful gateway payment produces two audit entries:

```text
Razorpay Payment SUCCESS
        ↓
PAYMENT_RECEIVED
        ↓
PaymentAllocation
        ↓
PAYMENT_ALLOCATED
```

Example:

```text
Payment Amount: ₹100

FT 1
Type: PAYMENT_RECEIVED
Amount: ₹100

FT 2
Type: PAYMENT_ALLOCATED
Amount: ₹100
```

Both financial transactions reference the same payment and bill but have separate financial transaction IDs and references.

### Financial Transaction Table

The `financial_transactions` table contains:

```text
financial_transaction_id
account_id
payment_id
bill_id
transaction_type
amount
reference
created_at
```

Financial transactions are designed as append-only audit records. Existing financial transactions are not updated or overwritten.

### Idempotency

Razorpay payment verification and the `payment.captured` webhook may both process the same successful payment.

Before creating a financial transaction, the application checks whether a financial transaction already exists for the same payment and transaction type

This prevents duplicate:

```text
PAYMENT_RECEIVED
PAYMENT_ALLOCATED
```

records when both payment callback paths execute.

### Database Migration

Financial transaction persistence was introduced using:

```text
V8__create_financial_transactions.sql
```

### Local Validation

A ₹20 bill was paid in full using Razorpay.

The resulting financial transactions were:

```text
PAYMENT_RECEIVED   ₹20
PAYMENT_ALLOCATED  ₹20
```

The resulting bill state was:

```text
Bill Total       = ₹20
Paid Amount      = ₹20
Remaining Amount = ₹0
Status           = PAID
```

Exactly two financial transaction rows were created for the payment.

### Cloud Validation

The Financial Transaction flow was also validated on Render using Neon PostgreSQL.

A ₹100 bill was paid through Razorpay.

Neon recorded exactly:

```text
PAYMENT_RECEIVED   ₹100
PAYMENT_ALLOCATED  ₹100
```

Both records referenced the same payment and bill while having separate financial transaction IDs and references.

The deployed flow was therefore validated as:

```text
Razorpay
   ↓
Payment SUCCESS
   ↓
PAYMENT_RECEIVED FT
   ↓
Payment Allocation
   ↓
PAYMENT_ALLOCATED FT
   ↓
Bill PAID
```

---

## Payment Reconciliation

The platform supports reconciliation between internal successful payments and Razorpay payment records.

Reconciliation verifies that the payment recorded internally matches the payment available at the gateway.

### Reconciliation Checks

The following values are compared:

```text
Internal Payment
        ↓
gatewayPaymentId
gatewayOrderId
amount
status

        VS

Razorpay Payment
        ↓
paymentId
orderId
amount
status
```

Possible reconciliation statuses are:

```text
MATCHED
AMOUNT_MISMATCH
STATUS_MISMATCH
ORDER_MISMATCH
PAYMENT_NOT_FOUND
```

### Manual Reconciliation

A payment can be reconciled manually using:

```text
POST /api/v1/payments/{paymentId}/reconcile
```

Only successful payments with a gateway payment ID are eligible for reconciliation.

Example:

```text
Internal Amount   = ₹100
Gateway Amount    = ₹100
Internal Status   = SUCCESS
Gateway Status    = captured
Order ID          = matching

Result            = MATCHED
```

### Reconciliation History

Every reconciliation attempt is stored as a separate record.

Previous results are not overwritten.

Example:

```text
Payment
   ↓
MATCHED
   ↓
AMOUNT_MISMATCH
   ↓
ORDER_MISMATCH
   ↓
MATCHED
```

This provides a historical audit trail of reconciliation activity.

### Scheduled Reconciliation

The application also performs scheduled reconciliation.

The scheduler:

```text
Find SUCCESS payments
        ↓
Check gateway payment ID
        ↓
Already MATCHED?
   yes → skip
   no  → reconcile
        ↓
Persist result
```

Payments whose latest reconciliation result is not `MATCHED` remain eligible for future reconciliation attempts.

This allows temporary problems such as `PAYMENT_NOT_FOUND` or mismatches to be retried automatically.

The reconciliation schedule and timezone are configurable through application configuration.

### Reconciliation Summary

The current reconciliation health can be retrieved using:

```text
GET /api/v1/payments/reconciliation/summary
```

The summary considers only the latest reconciliation result for each payment.

Example:

```json
{
  "matched": 5,
  "amountMismatch": 0,
  "statusMismatch": 0,
  "orderMismatch": 0,
  "paymentNotFound": 1
}
```

Historical mismatch records therefore do not affect the current operational summary after a payment later becomes `MATCHED`.

### Current Reconciliation Exceptions

Payments currently requiring attention can be retrieved using:

```text
GET /api/v1/payments/reconciliation/exceptions
```

Only payments whose latest reconciliation result is not `MATCHED` are returned.

### Payment Reconciliation History

The complete reconciliation history for an individual payment can be retrieved using:

```text
GET /api/v1/payments/{paymentId}/reconciliation/history
```

Results are returned newest first.

### Database Migration

Reconciliation persistence was introduced using:

```text
V9__create_payment_reconciliations.sql
```

The `payment_reconciliations` table stores values such as:

```text
reconciliation_id
payment_id
gateway_payment_id
internal_amount
gateway_amount
internal_status
gateway_status
reconciliation_status
reconciled_at
```

### Validation

The reconciliation flow was validated for:

```text
MATCHED             ✅
PAYMENT_NOT_FOUND   ✅
AMOUNT_MISMATCH     ✅
ORDER_MISMATCH      ✅
STATUS_MISMATCH     ✅
```

`MATCHED`, payment lookup failure, amount mismatch, and order mismatch were validated using controlled local payment scenarios.

`STATUS_MISMATCH` was validated using a JUnit and Mockito unit test with a simulated non-captured gateway payment.

- Razorpay payment reconciliation
- Scheduled reconciliation with retry handling
- Reconciliation summary and exception reporting
- Per-payment reconciliation audit history