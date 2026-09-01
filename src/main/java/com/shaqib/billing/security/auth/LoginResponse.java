package com.shaqib.billing.security.auth;

public record LoginResponse(
        String email,
        String role,
        String token
) {
}