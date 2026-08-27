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

Before creating a financial transaction, the application checks whether the same payment already contains that financial transaction type.

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