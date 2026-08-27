package com.shaqib.billing.reconciliation.dto;

public record ReconciliationSummaryResponse(
        long matched,
        long amountMismatch,
        long statusMismatch,
        long orderMismatch,
        long paymentNotFound
) {
}