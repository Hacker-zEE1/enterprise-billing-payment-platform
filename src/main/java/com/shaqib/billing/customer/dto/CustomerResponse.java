package com.shaqib.billing.customer.dto;

import com.shaqib.billing.customer.entity.CustomerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID customerId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        CustomerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}