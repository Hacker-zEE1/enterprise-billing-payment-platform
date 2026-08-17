package com.shaqib.billing.account.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountNumberGenerator {

    public String generate() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "ACC-" + randomPart;
    }
}