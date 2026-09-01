package com.shaqib.billing.security.authorization;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.account.repository.AccountRepository;
import com.shaqib.billing.security.user.AppUser;
import com.shaqib.billing.security.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountAuthorizationService {

    private final AppUserRepository appUserRepository;
    private final AccountRepository accountRepository;

    public AccountAuthorizationService(
            AppUserRepository appUserRepository,
            AccountRepository accountRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.accountRepository = accountRepository;
    }

    public boolean canAccessAccount(
            Authentication authentication,
            UUID accountId
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
                        new IllegalArgumentException("Authenticated user not found")
                );

        if (user.getCustomer() == null) {
            return false;
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found: " + accountId
                        )
                );

        return account.getCustomer()
                .getCustomerId()
                .equals(
                        user.getCustomer().getCustomerId()
                );
    }
}