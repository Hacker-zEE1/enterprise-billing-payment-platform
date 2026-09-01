package com.shaqib.billing.security.auth;

import com.shaqib.billing.security.role.Role;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        Role role,
        boolean enabled
) {
}