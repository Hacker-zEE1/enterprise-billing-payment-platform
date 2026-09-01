package com.shaqib.billing.security.user;

import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.security.registration.RegistrationToken;
import com.shaqib.billing.security.registration.RegistrationTokenService;
import com.shaqib.billing.security.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegistrationTokenService registrationTokenService;

    private AppUserService appUserService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        appUserService =
                new AppUserService(
                        appUserRepository,
                        passwordEncoder,
                        registrationTokenService
                );
    }

    @Test
    void validRegistrationTokenCreatesCustomerUserAndConsumesToken() {

        String rawToken = "valid-registration-token";
        String rawPassword = "Password123";
        String encodedPassword = "encoded-password";

        UUID customerId = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        when(customer.getCustomerId())
                .thenReturn(customerId);

        when(customer.getEmail())
                .thenReturn("customer@example.com");

        RegistrationToken registrationToken =
                new RegistrationToken(
                        UUID.randomUUID(),
                        customer,
                        "token-hash",
                        LocalDateTime.now().plusHours(1),
                        false,
                        LocalDateTime.now()
                );

        when(registrationTokenService
                .validateTokenForRegistration(rawToken))
                .thenReturn(registrationToken);

        when(appUserRepository
                .existsByCustomerCustomerId(customerId))
                .thenReturn(false);

        when(appUserRepository
                .existsByEmail("customer@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(rawPassword))
                .thenReturn(encodedPassword);

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        AppUser result =
                appUserService.register(
                        rawToken,
                        rawPassword
                );

        assertNotNull(result);
        assertEquals(
                "customer@example.com",
                result.getEmail()
        );
        assertEquals(
                encodedPassword,
                result.getPassword()
        );
        assertEquals(
                Role.CUSTOMER,
                result.getRole()
        );
        assertTrue(result.isEnabled());
        assertSame(
                customer,
                result.getCustomer()
        );

        verify(passwordEncoder)
                .encode(rawPassword);

        verify(registrationTokenService)
                .consumeToken(registrationToken);

        verify(appUserRepository)
                .save(any(AppUser.class));
    }

    @Test
    void registrationIsRejectedWhenCustomerAlreadyHasUser() {

        String rawToken = "valid-registration-token";

        UUID customerId = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        when(customer.getCustomerId())
                .thenReturn(customerId);

        RegistrationToken registrationToken =
                new RegistrationToken(
                        UUID.randomUUID(),
                        customer,
                        "token-hash",
                        LocalDateTime.now().plusHours(1),
                        false,
                        LocalDateTime.now()
                );

        when(registrationTokenService
                .validateTokenForRegistration(rawToken))
                .thenReturn(registrationToken);

        when(appUserRepository
                .existsByCustomerCustomerId(customerId))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> appUserService.register(
                                rawToken,
                                "Password123"
                        )
                );

        assertEquals(
                "Customer already has a user account",
                exception.getMessage()
        );

        verify(appUserRepository, never())
                .save(any());

        verify(registrationTokenService, never())
                .consumeToken(registrationToken);
    }

    @Test
    void registrationIsRejectedWhenEmailAlreadyExists() {

        String rawToken = "valid-registration-token";

        UUID customerId = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        when(customer.getCustomerId())
                .thenReturn(customerId);

        when(customer.getEmail())
                .thenReturn("existing@example.com");

        RegistrationToken registrationToken =
                new RegistrationToken(
                        UUID.randomUUID(),
                        customer,
                        "token-hash",
                        LocalDateTime.now().plusHours(1),
                        false,
                        LocalDateTime.now()
                );

        when(registrationTokenService
                .validateTokenForRegistration(rawToken))
                .thenReturn(registrationToken);

        when(appUserRepository
                .existsByCustomerCustomerId(customerId))
                .thenReturn(false);

        when(appUserRepository
                .existsByEmail("existing@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> appUserService.register(
                                rawToken,
                                "Password123"
                        )
                );

        assertEquals(
                "User already exists with email: existing@example.com",
                exception.getMessage()
        );

        verify(appUserRepository, never())
                .save(any());

        verify(registrationTokenService, never())
                .consumeToken(registrationToken);
    }


}