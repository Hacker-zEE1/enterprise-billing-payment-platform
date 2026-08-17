package com.shaqib.billing.account.dto;

import com.shaqib.billing.account.entity.AccountStatus;
import com.shaqib.billing.account.entity.AccountType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        UUID customerId,
        String accountNumber,
        AccountType accountType,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}