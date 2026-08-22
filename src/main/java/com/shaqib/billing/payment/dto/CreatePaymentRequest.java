package com.shaqib.billing.payment.dto;

import com.shaqib.billing.payment.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod

) {
}