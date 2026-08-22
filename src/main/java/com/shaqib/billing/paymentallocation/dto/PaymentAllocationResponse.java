package com.shaqib.billing.paymentallocation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentAllocationResponse(
        UUID allocationId,
        UUID paymentId,
        UUID billId,
        BigDecimal allocatedAmount,
        LocalDateTime createdAt
) {
}