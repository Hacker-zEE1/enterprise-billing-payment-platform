package com.shaqib.billing.account.repository;

import com.shaqib.billing.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountIdAndCustomerCustomerId(
            UUID accountId,
            UUID customerId
    );

    List<Account> findAllByCustomerCustomerId(UUID customerId);

}