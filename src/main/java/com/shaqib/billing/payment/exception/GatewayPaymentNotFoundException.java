package com.shaqib.billing.payment.exception;

public class GatewayPaymentNotFoundException extends RuntimeException {

    public GatewayPaymentNotFoundException(String message) {
        super(message);
    }
}