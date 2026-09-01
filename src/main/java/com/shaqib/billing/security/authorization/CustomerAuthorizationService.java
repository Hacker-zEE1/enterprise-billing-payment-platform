package com.shaqib.billing.security.authorization;

import com.shaqib.billing.security.user.AppUser;
import com.shaqib.billing.security.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerAuthorizationService {

    private final AppUserRepository appUserRepository;

    public CustomerAuthorizationService(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    public boolean canAccessCustomer(
            Authentication authentication,
            UUID customerId
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (isAdmin) {
            return true;
        }

        AppUser user = appUserRepository.findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );

        if (user.getCustomer() == null) {
            return false;
        }

        return user.getCustomer()
                .getCustomerId()
                .equals(customerId);
    }
}