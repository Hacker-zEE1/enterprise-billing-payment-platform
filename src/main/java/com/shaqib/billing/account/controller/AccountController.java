package com.shaqib.billing.account.controller;

import com.shaqib.billing.account.dto.AccountResponse;
import com.shaqib.billing.account.dto.CreateAccountRequest;
import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getCustomer().getCustomerId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAccountRequest request
    ) {

        Account account = accountService.createAccount(
                customerId,
                request.accountType()
        );

        AccountResponse response = toResponse(account);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{accountId}")
    @PreAuthorize(
            "@customerAuthorizationService.canAccessCustomer(authentication, #customerId)"
    )
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable UUID customerId,
            @PathVariable UUID accountId
    ) {

        Account account = accountService.getAccountById(
                customerId,
                accountId
        );

        AccountResponse response = toResponse(account);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    @PreAuthorize(
            "@customerAuthorizationService.canAccessCustomer(authentication, #customerId)"
    )
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(
            @PathVariable UUID customerId
    ) {

        List<AccountResponse> responses = accountService
                .getAccountsByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }


    @PatchMapping("/{accountId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> deactivateAccount(
            @PathVariable UUID customerId,
            @PathVariable UUID accountId
    ) {

        Account account = accountService.deactivateAccount(
                customerId,
                accountId
        );

        return ResponseEntity.ok(toResponse(account));
    }

    @PatchMapping("/{accountId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> activateAccount(
            @PathVariable UUID customerId,
            @PathVariable UUID accountId
    ) {

        Account account = accountService.activateAccount(
                customerId,
                accountId
        );

        return ResponseEntity.ok(toResponse(account));
    }
}