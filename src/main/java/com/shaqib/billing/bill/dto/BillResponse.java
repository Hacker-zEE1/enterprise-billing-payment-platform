package com.shaqib.billing.bill.dto;

import com.shaqib.billing.bill.entity.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BillResponse(
        UUID billId,
        UUID accountId,
        String billNumber,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BillStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}