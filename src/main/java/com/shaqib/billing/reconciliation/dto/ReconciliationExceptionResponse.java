package com.shaqib.billing.reconciliation.dto;

import com.shaqib.billing.reconciliation.entity.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReconciliationExceptionResponse(
        UUID reconciliationId,
        UUID paymentId,
        String gatewayPaymentId,
        BigDecimal internalAmount,
        BigDecimal gatewayAmount,
        String internalStatus,
        String gatewayStatus,
        ReconciliationStatus reconciliationStatus,
        LocalDateTime reconciledAt
) {
}