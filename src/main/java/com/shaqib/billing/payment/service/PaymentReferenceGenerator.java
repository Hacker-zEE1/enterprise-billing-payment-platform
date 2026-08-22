package com.shaqib.billing.payment.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentReferenceGenerator {

    public String generate() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "PAY-" + randomPart;
    }
}