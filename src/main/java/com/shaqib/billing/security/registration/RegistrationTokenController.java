package com.shaqib.billing.security.registration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/registration-token")
public class RegistrationTokenController {

    private final RegistrationTokenService registrationTokenService;

    public RegistrationTokenController(
            RegistrationTokenService registrationTokenService
    ) {
        this.registrationTokenService = registrationTokenService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationTokenResponse> createRegistrationToken(
            @PathVariable UUID customerId
    ) {

        String token =
                registrationTokenService.createToken(customerId);

        RegistrationTokenResponse response =
                new RegistrationTokenResponse(token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}