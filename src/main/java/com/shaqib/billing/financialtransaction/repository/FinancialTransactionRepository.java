package com.shaqib.billing.financialtransaction.repository;

import com.shaqib.billing.financialtransaction.entity.FinancialTransaction;
import com.shaqib.billing.financialtransaction.entity.FinancialTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialTransactionRepository
        extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findAllByAccountAccountId(UUID accountId);

    List<FinancialTransaction> findAllByPaymentPaymentId(UUID paymentId);

    List<FinancialTransaction> findAllByBillBillId(UUID billId);

    boolean existsByPaymentPaymentIdAndTransactionType(
            UUID paymentId,
            FinancialTransactionType transactionType
    );

    Optional<FinancialTransaction> findByPaymentPaymentIdAndTransactionType(
            UUID paymentId,
            FinancialTransactionType transactionType
    );
}