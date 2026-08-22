package com.shaqib.billing.paymentallocation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentAllocationRequest(

        @NotNull(message = "Bill id is required")
        UUID billId,

        @NotNull(message = "Allocated amount is required")
        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "Allocated amount must be greater than zero"
        )
        BigDecimal allocatedAmount

) {
}