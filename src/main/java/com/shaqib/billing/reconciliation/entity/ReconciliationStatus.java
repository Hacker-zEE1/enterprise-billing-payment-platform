package com.shaqib.billing.reconciliation.entity;

public enum ReconciliationStatus {
    MATCHED,
    AMOUNT_MISMATCH,
    STATUS_MISMATCH,
    ORDER_MISMATCH,
    PAYMENT_NOT_FOUND
}