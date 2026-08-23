package com.shaqib.billing.bill.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayableBillResponse(
        UUID billId,
        String billNumber,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        LocalDate dueDate
) {
}