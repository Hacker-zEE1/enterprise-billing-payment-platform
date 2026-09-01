package com.shaqib.billing.security.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        String registrationToken,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}