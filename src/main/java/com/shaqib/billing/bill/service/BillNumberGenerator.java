package com.shaqib.billing.bill.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BillNumberGenerator {

    public String generate() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "BILL-" + randomPart;
    }
}