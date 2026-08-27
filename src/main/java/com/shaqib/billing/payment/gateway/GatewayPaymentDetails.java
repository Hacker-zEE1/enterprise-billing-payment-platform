package com.shaqib.billing.payment.gateway;

import java.math.BigDecimal;

public record GatewayPaymentDetails(
        String gatewayPaymentId,
        String gatewayOrderId,
        BigDecimal amount,
        String status
) {
}