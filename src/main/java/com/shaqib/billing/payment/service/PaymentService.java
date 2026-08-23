package com.shaqib.billing.payment.service;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.account.exception.AccountNotFoundException;
import com.shaqib.billing.account.repository.AccountRepository;
import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.entity.PaymentMethod;
import com.shaqib.billing.payment.entity.PaymentStatus;
import com.shaqib.billing.payment.exception.InvalidPaymentStatusTransitionException;
import com.shaqib.billing.payment.exception.PaymentNotFoundException;
import com.shaqib.billing.payment.gateway.GatewayOrderResponse;
import com.shaqib.billing.payment.gateway.PaymentGateway;
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
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentRepository paymentRepository,
            AccountRepository accountRepository,
            PaymentReferenceGenerator paymentReferenceGenerator,
            PaymentGateway paymentGateway
    ) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.paymentReferenceGenerator = paymentReferenceGenerator;
        this.paymentGateway = paymentGateway;
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

        String paymentReference =
                paymentReferenceGenerator.generate();

        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment(
                UUID.randomUUID(),
                account,
                paymentReference,
                amount,
                paymentMethod,
                PaymentStatus.PENDING,
                now,
                now,
                now
        );

        GatewayOrderResponse gatewayOrder =
                paymentGateway.createOrder(
                        amount,
                        "INR",
                        paymentReference
                );

        payment.assignGatewayOrder(
                gatewayOrder.gateway(),
                gatewayOrder.gatewayOrderId(),
                LocalDateTime.now()
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


    public Payment verifyPayment(
            UUID accountId,
            UUID paymentId,
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    ) {

        Payment payment = getPaymentById(accountId, paymentId);

        if (!gatewayOrderId.equals(payment.getGatewayOrderId())) {
            throw new InvalidPaymentException(
                    "Gateway order ID does not match the payment"
            );
        }

        boolean valid = paymentGateway.verifyPayment(
                gatewayOrderId,
                gatewayPaymentId,
                signature
        );

        if (!valid) {
            throw new InvalidPaymentException(
                    "Invalid payment signature"
            );
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (gatewayPaymentId.equals(payment.getGatewayPaymentId())) {
                return payment;
            }

            throw new InvalidPaymentException(
                    "Payment already completed with a different gateway payment"
            );
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only PENDING payments can be verified"
            );
        }

        payment.completeGatewayPayment(
                gatewayPaymentId,
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }

    public Payment processCapturedPaymentWebhook(
            String gatewayOrderId,
            String gatewayPaymentId
    ) {

        Payment payment = paymentRepository
                .findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for gateway order id: "
                                + gatewayOrderId
                ));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            if (gatewayPaymentId.equals(payment.getGatewayPaymentId())) {
                return payment;
            }

            throw new InvalidPaymentException(
                    "Payment already completed with a different gateway payment ID"
            );
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only PENDING payments can be completed from webhook"
            );
        }

        payment.completeGatewayPayment(
                gatewayPaymentId,
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }

}