package com.shaqib.billing.financialtransaction.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FinancialTransactionReferenceGenerator {

    public String generate() {

        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "FT-" + randomPart;
    }
}