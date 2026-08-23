
### 2. Create `docs/payment-flow.md`

```markdown
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