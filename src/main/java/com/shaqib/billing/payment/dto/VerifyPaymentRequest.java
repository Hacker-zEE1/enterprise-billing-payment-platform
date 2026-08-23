package com.shaqib.billing.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(

        @NotBlank
        String gatewayOrderId,

        @NotBlank
        String gatewayPaymentId,

        @NotBlank
        String signature
) {
}