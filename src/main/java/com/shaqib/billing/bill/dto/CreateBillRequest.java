package com.shaqib.billing.bill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBillRequest(

        @NotNull(message = "Billing period start is required")
        LocalDate billingPeriodStart,

        @NotNull(message = "Billing period end is required")
        LocalDate billingPeriodEnd,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.00", inclusive = true,
                message = "Total amount must be zero or greater")
        BigDecimal totalAmount

) {
}