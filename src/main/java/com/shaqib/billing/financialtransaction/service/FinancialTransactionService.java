package com.shaqib.billing.financialtransaction.service;

import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.financialtransaction.entity.FinancialTransaction;
import com.shaqib.billing.financialtransaction.entity.FinancialTransactionType;
import com.shaqib.billing.financialtransaction.repository.FinancialTransactionRepository;
import com.shaqib.billing.payment.entity.Payment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FinancialTransactionService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionReferenceGenerator referenceGenerator;

    public FinancialTransactionService(
            FinancialTransactionRepository financialTransactionRepository,
            FinancialTransactionReferenceGenerator referenceGenerator
    ) {
        this.financialTransactionRepository = financialTransactionRepository;
        this.referenceGenerator = referenceGenerator;
    }

    public FinancialTransaction recordPaymentReceived(
            Payment payment
    ) {

        if (financialTransactionRepository
                .existsByPaymentPaymentIdAndTransactionType(
                        payment.getPaymentId(),
                        FinancialTransactionType.PAYMENT_RECEIVED
                )) {

            return financialTransactionRepository
                    .findByPaymentPaymentIdAndTransactionType(
                            payment.getPaymentId(),
                            FinancialTransactionType.PAYMENT_RECEIVED
                    )
                    .orElseThrow();
        }

        FinancialTransaction financialTransaction =
                new FinancialTransaction(
                        UUID.randomUUID(),
                        payment.getAccount(),
                        payment,
                        payment.getBill(),
                        FinancialTransactionType.PAYMENT_RECEIVED,
                        payment.getAmount(),
                        referenceGenerator.generate(),
                        LocalDateTime.now()
                );

        return financialTransactionRepository.save(financialTransaction);
    }

    public FinancialTransaction recordPaymentAllocated(
            Payment payment,
            Bill bill
    ) {

        if (financialTransactionRepository
                .existsByPaymentPaymentIdAndTransactionType(
                        payment.getPaymentId(),
                        FinancialTransactionType.PAYMENT_ALLOCATED
                )) {

            return financialTransactionRepository
                    .findByPaymentPaymentIdAndTransactionType(
                            payment.getPaymentId(),
                            FinancialTransactionType.PAYMENT_ALLOCATED
                    )
                    .orElseThrow();
        }

        FinancialTransaction financialTransaction =
                new FinancialTransaction(
                        UUID.randomUUID(),
                        payment.getAccount(),
                        payment,
                        bill,
                        FinancialTransactionType.PAYMENT_ALLOCATED,
                        payment.getAmount(),
                        referenceGenerator.generate(),
                        LocalDateTime.now()
                );

        return financialTransactionRepository.save(financialTransaction);
    }
}