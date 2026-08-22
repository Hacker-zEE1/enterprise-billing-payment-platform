package com.shaqib.billing.payment.service;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.account.exception.AccountNotFoundException;
import com.shaqib.billing.account.repository.AccountRepository;
import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.entity.PaymentMethod;
import com.shaqib.billing.payment.entity.PaymentStatus;
import com.shaqib.billing.payment.exception.PaymentNotFoundException;
import com.shaqib.billing.payment.repository.PaymentRepository;
import com.shaqib.billing.payment.exception.InvalidPaymentException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentReferenceGenerator paymentReferenceGenerator;

    public PaymentService(
            PaymentRepository paymentRepository,
            AccountRepository accountRepository,
            PaymentReferenceGenerator paymentReferenceGenerator
    ) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.paymentReferenceGenerator = paymentReferenceGenerator;
    }

    public Payment createPayment(
            UUID accountId,
            BigDecimal amount,
            PaymentMethod paymentMethod
    ) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + accountId
                        )
                );

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException(
                    "Payment amount must be greater than zero"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment(
                UUID.randomUUID(),
                account,
                paymentReferenceGenerator.generate(),
                amount,
                paymentMethod,
                PaymentStatus.PENDING,
                now,
                now,
                now
        );

        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(
            UUID accountId,
            UUID paymentId
    ) {
        return paymentRepository
                .findByPaymentIdAndAccountAccountId(paymentId, accountId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId
                        )
                );
    }

    public List<Payment> getPaymentsByAccountId(UUID accountId) {

        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(
                    "Account not found with id: " + accountId
            );
        }

        return paymentRepository.findAllByAccountAccountId(accountId);
    }


    public Payment markPaymentSuccess(
            UUID accountId,
            UUID paymentId
    ) {

        Payment payment = getPaymentById(accountId, paymentId);

        payment.markSuccess(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment markPaymentFailed(
            UUID accountId,
            UUID paymentId
    ) {

        Payment payment = getPaymentById(accountId, paymentId);

        payment.markFailed(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment cancelPayment(
            UUID accountId,
            UUID paymentId
    ) {

        Payment payment = getPaymentById(accountId, paymentId);

        payment.cancel(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

}