package com.shaqib.billing.payment.gateway;

import com.shaqib.billing.payment.entity.PaymentGatewayProvider;

public record GatewayOrderResponse(
        String gatewayOrderId,
        PaymentGatewayProvider gateway,
        String status
) {
}