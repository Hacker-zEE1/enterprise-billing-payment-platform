package com.shaqib.billing.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    GatewayOrderResponse createOrder(
            BigDecimal amount,
            String currency,
            String receipt
    );

    boolean verifyPayment(
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    );

    boolean verifyWebhook(
            String payload,
            String signature
    );
}