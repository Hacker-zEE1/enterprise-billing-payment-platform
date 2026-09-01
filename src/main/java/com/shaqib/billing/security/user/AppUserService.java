package com.shaqib.billing.security.user;

import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.security.role.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.shaqib.billing.security.registration.RegistrationToken;
import com.shaqib.billing.security.registration.RegistrationTokenService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationTokenService registrationTokenService;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            RegistrationTokenService registrationTokenService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrationTokenService = registrationTokenService;
    }

    @Transactional
    public AppUser register(
            String rawToken,
            String rawPassword
    ) {

        RegistrationToken registrationToken =
                registrationTokenService
                        .validateTokenForRegistration(rawToken);

        Customer customer = registrationToken.getCustomer();

        UUID customerId = customer.getCustomerId();

        if (appUserRepository.existsByCustomerCustomerId(customerId)) {
            throw new IllegalArgumentException(
                    "Customer already has a user account"
            );
        }

        String email = customer.getEmail();

        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "User already exists with email: " + email
            );
        }

        AppUser user = new AppUser(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(rawPassword),
                Role.CUSTOMER,
                true,
                LocalDateTime.now(),
                customer
        );

        AppUser savedUser =
                appUserRepository.save(user);

        registrationTokenService.consumeToken(
                registrationToken
        );

        return savedUser;
    }
}