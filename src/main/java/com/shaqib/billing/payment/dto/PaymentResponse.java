package com.shaqib.billing.payment.dto;

import com.shaqib.billing.payment.entity.PaymentGatewayProvider;
import com.shaqib.billing.payment.entity.PaymentMethod;
import com.shaqib.billing.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID accountId,
        String paymentReference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        PaymentGatewayProvider gateway,
        String gatewayOrderId,
        String gatewayPaymentId,
        LocalDateTime paymentDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}