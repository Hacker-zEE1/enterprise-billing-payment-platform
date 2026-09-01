package com.shaqib.billing.security.registration;

import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.customer.repository.CustomerRepository;
import com.shaqib.billing.security.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationTokenServiceTest {

    @Mock
    private RegistrationTokenRepository registrationTokenRepository;

    @Mock
    private CustomerRepository customerRepository;

    private RegistrationTokenService registrationTokenService;

    @Mock
    private AppUserRepository appUserRepository;
    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        registrationTokenService =
                new RegistrationTokenService(
                        registrationTokenRepository,
                        customerRepository,
                        appUserRepository
                );
    }

    @Test
    void validTokenIsAccepted() {

        String rawToken = "valid-registration-token";

        String tokenHash =
                registrationTokenService.hashToken(rawToken);

        RegistrationToken token =
                new RegistrationToken(
                        UUID.randomUUID(),
                        mock(Customer.class),
                        tokenHash,
                        LocalDateTime.now().plusHours(1),
                        false,
                        LocalDateTime.now()
                );

        when(registrationTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        RegistrationToken result =
                registrationTokenService.validateToken(rawToken);

        assertSame(token, result);

        verify(registrationTokenRepository)
                .findByTokenHash(tokenHash);
    }

    @Test
    void invalidTokenIsRejected() {

        String rawToken = "invalid-registration-token";

        String tokenHash =
                registrationTokenService.hashToken(rawToken);

        when(registrationTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> registrationTokenService
                                .validateToken(rawToken)
                );

        assertEquals(
                "Invalid registration token",
                exception.getMessage()
        );
    }

    @Test
    void usedTokenIsRejected() {

        String rawToken = "used-registration-token";

        String tokenHash =
                registrationTokenService.hashToken(rawToken);

        RegistrationToken token =
                new RegistrationToken(
                        UUID.randomUUID(),
                        mock(Customer.class),
                        tokenHash,
                        LocalDateTime.now().plusHours(1),
                        true,
                        LocalDateTime.now()
                );

        when(registrationTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> registrationTokenService
                                .validateToken(rawToken)
                );

        assertEquals(
                "Registration token has already been used",
                exception.getMessage()
        );
    }

    @Test
    void expiredTokenIsRejected() {

        String rawToken = "expired-registration-token";

        String tokenHash =
                registrationTokenService.hashToken(rawToken);

        RegistrationToken token =
                new RegistrationToken(
                        UUID.randomUUID(),
                        mock(Customer.class),
                        tokenHash,
                        LocalDateTime.now().minusMinutes(1),
                        false,
                        LocalDateTime.now().minusHours(2)
                );

        when(registrationTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> registrationTokenService
                                .validateToken(rawToken)
                );

        assertEquals(
                "Registration token has expired",
                exception.getMessage()
        );
    }

    @Test
    void consumeTokenMarksTokenAsUsed() {

        RegistrationToken token =
                new RegistrationToken(
                        UUID.randomUUID(),
                        mock(Customer.class),
                        "token-hash",
                        LocalDateTime.now().plusHours(1),
                        false,
                        LocalDateTime.now()
                );

        when(registrationTokenRepository.save(token))
                .thenReturn(token);

        RegistrationToken result =
                registrationTokenService.consumeToken(token);

        assertTrue(result.isUsed());

        verify(registrationTokenRepository)
                .save(token);
    }

    @Test
    void tokenIsNotCreatedWhenCustomerAlreadyHasUser() {

        UUID customerId = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(appUserRepository
                .existsByCustomerCustomerId(customerId))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> registrationTokenService
                                .createToken(customerId)
                );

        assertEquals(
                "Customer already has a user account",
                exception.getMessage()
        );

        verify(registrationTokenRepository, never())
                .save(any());
    }

    @Test
    void tokenIsNotCreatedWhenActiveTokenAlreadyExists() {

        UUID customerId = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(appUserRepository
                .existsByCustomerCustomerId(customerId))
                .thenReturn(false);

        when(registrationTokenRepository
                .existsByCustomerCustomerIdAndUsedFalseAndExpiresAtAfter(
                        eq(customerId),
                        any(LocalDateTime.class)
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> registrationTokenService
                                .createToken(customerId)
                );

        assertEquals(
                "Active registration token already exists for customer",
                exception.getMessage()
        );

        verify(registrationTokenRepository, never())
                .save(any());
    }

    @Test
    void registrationValidationUsesLockedTokenLookup() {

        String rawToken = "locked-registration-token";

        String tokenHash =
                registrationTokenService.hashToken(rawToken);

        RegistrationToken token =
                new RegistrationToken(
                        UUID.randomUUID(),
                        mock(Customer.class),
                        tokenHash,
                        LocalDateTime.now().plusHours(1),
                        false,
                        LocalDateTime.now()
                );

        when(registrationTokenRepository
                .findByTokenHashForUpdate(tokenHash))
                .thenReturn(Optional.of(token));

        RegistrationToken result =
                registrationTokenService
                        .validateTokenForRegistration(rawToken);

        assertSame(token, result);

        verify(registrationTokenRepository)
                .findByTokenHashForUpdate(tokenHash);
    }

}