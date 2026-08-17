package com.shaqib.billing.account.service;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.account.entity.AccountStatus;
import com.shaqib.billing.account.entity.AccountType;
import com.shaqib.billing.account.exception.AccountNotFoundException;
import com.shaqib.billing.account.repository.AccountRepository;
import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.customer.exception.CustomerNotFoundException;
import com.shaqib.billing.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            AccountNumberGenerator accountNumberGenerator
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    public Account createAccount(
            UUID customerId,
            AccountType accountType
    ) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        Account account = new Account(
                UUID.randomUUID(),
                customer,
                accountNumberGenerator.generate(),
                accountType,
                AccountStatus.ACTIVE,
                now,
                now
        );

        return accountRepository.save(account);
    }


    public Account getAccountById(
            UUID customerId,
            UUID accountId
    ) {
        return getAccountForCustomer(customerId, accountId);
    }


    public List<Account> getAccountsByCustomerId(UUID customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(
                    "Customer not found with id: " + customerId
            );
        }

        return accountRepository.findAllByCustomerCustomerId(customerId);
    }


    public Account deactivateAccount(
            UUID customerId,
            UUID accountId
    ) {

        Account account = getAccountForCustomer(customerId, accountId);

        account.deactivate(LocalDateTime.now());

        return accountRepository.save(account);
    }

    public Account activateAccount(
            UUID customerId,
            UUID accountId
    ) {

        Account account = getAccountForCustomer(customerId, accountId);

        account.activate(LocalDateTime.now());

        return accountRepository.save(account);
    }


    private Account getAccountForCustomer(
            UUID customerId,
            UUID accountId
    ) {
        return accountRepository
                .findByAccountIdAndCustomerCustomerId(accountId, customerId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + accountId
                        )
                );
    }
}