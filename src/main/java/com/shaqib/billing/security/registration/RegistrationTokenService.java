package com.shaqib.billing.security.registration;

import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.customer.repository.CustomerRepository;
import com.shaqib.billing.security.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RegistrationTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_VALID_HOURS = 24;

    private final RegistrationTokenRepository registrationTokenRepository;
    private final CustomerRepository customerRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    private final AppUserRepository appUserRepository;

    public RegistrationTokenService(
            RegistrationTokenRepository registrationTokenRepository,
            CustomerRepository customerRepository,
            AppUserRepository appUserRepository
    ) {
        this.registrationTokenRepository = registrationTokenRepository;
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public String createToken(UUID customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found: " + customerId
                        )
                );

        if (appUserRepository.existsByCustomerCustomerId(customerId)) {
            throw new IllegalArgumentException(
                    "Customer already has a user account"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (registrationTokenRepository
                .existsByCustomerCustomerIdAndUsedFalseAndExpiresAtAfter(
                        customerId,
                        now
                )) {
            throw new IllegalArgumentException(
                    "Active registration token already exists for customer"
            );
        }

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        RegistrationToken registrationToken =
                new RegistrationToken(
                        UUID.randomUUID(),
                        customer,
                        tokenHash,
                        now.plusHours(TOKEN_VALID_HOURS),
                        false,
                        now
                );

        registrationTokenRepository.save(registrationToken);

        return rawToken;
    }

    public String hashToken(String rawToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }

    private String generateRawToken() {

        byte[] bytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Transactional(readOnly = true)
    public RegistrationToken validateToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Registration token is required"
            );
        }

        String tokenHash = hashToken(rawToken);

        RegistrationToken token =
                registrationTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid registration token"
                                )
                        );

        if (token.isUsed()) {
            throw new IllegalArgumentException(
                    "Registration token has already been used"
            );
        }

        if (token.isExpired()) {
            throw new IllegalArgumentException(
                    "Registration token has expired"
            );
        }

        return token;
    }

    @Transactional
    public RegistrationToken consumeToken(
            RegistrationToken token
    ) {

        if (token == null) {
            throw new IllegalArgumentException(
                    "Registration token is required"
            );
        }

        if (token.isUsed()) {
            throw new IllegalArgumentException(
                    "Registration token has already been used"
            );
        }

        if (token.isExpired()) {
            throw new IllegalArgumentException(
                    "Registration token has expired"
            );
        }

        token.markUsed();

        return registrationTokenRepository.save(token);
    }

    @Transactional
    public RegistrationToken validateTokenForRegistration(
            String rawToken
    ) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Registration token is required"
            );
        }

        String tokenHash = hashToken(rawToken);

        RegistrationToken token =
                registrationTokenRepository
                        .findByTokenHashForUpdate(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid registration token"
                                )
                        );

        if (token.isUsed()) {
            throw new IllegalArgumentException(
                    "Registration token has already been used"
            );
        }

        if (token.isExpired()) {
            throw new IllegalArgumentException(
                    "Registration token has expired"
            );
        }

        return token;
    }
}