package com.shaqib.billing.reconciliation.service;

import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.entity.PaymentStatus;
import com.shaqib.billing.payment.exception.GatewayPaymentNotFoundException;
import com.shaqib.billing.payment.exception.InvalidPaymentException;
import com.shaqib.billing.payment.exception.PaymentNotFoundException;
import com.shaqib.billing.payment.gateway.GatewayPaymentDetails;
import com.shaqib.billing.payment.gateway.PaymentGateway;
import com.shaqib.billing.payment.repository.PaymentRepository;
import com.shaqib.billing.reconciliation.dto.PaymentReconciliationResponse;
import com.shaqib.billing.reconciliation.dto.ReconciliationExceptionResponse;
import com.shaqib.billing.reconciliation.dto.ReconciliationSummaryResponse;
import com.shaqib.billing.reconciliation.entity.PaymentReconciliation;
import com.shaqib.billing.reconciliation.entity.ReconciliationStatus;
import com.shaqib.billing.reconciliation.repository.PaymentReconciliationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class PaymentReconciliationService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentReconciliationRepository reconciliationRepository;
    private static final Logger logger =
            LoggerFactory.getLogger(PaymentReconciliationService.class);
    public PaymentReconciliationService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            PaymentReconciliationRepository reconciliationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.reconciliationRepository = reconciliationRepository;
    }

    public PaymentReconciliation reconcilePayment(
            UUID paymentId
    ) {

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId
                        )
                );

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentException(
                    "Only SUCCESS payments can be reconciled"
            );
        }

        if (payment.getGatewayPaymentId() == null) {
            throw new InvalidPaymentException(
                    "Gateway payment ID is missing"
            );
        }

        GatewayPaymentDetails gatewayPayment;

        try {
            gatewayPayment =
                    paymentGateway.fetchPayment(
                            payment.getGatewayPaymentId()
                    );

        } catch (GatewayPaymentNotFoundException ex) {

            PaymentReconciliation reconciliation =
                    new PaymentReconciliation(
                            UUID.randomUUID(),
                            payment,
                            payment.getGatewayPaymentId(),
                            payment.getAmount(),
                            null,
                            payment.getStatus().name(),
                            null,
                            ReconciliationStatus.PAYMENT_NOT_FOUND,
                            LocalDateTime.now()
                    );

            return reconciliationRepository.save(
                    reconciliation
            );
        }

        ReconciliationStatus reconciliationStatus =
                determineStatus(
                        payment,
                        gatewayPayment
                );

        PaymentReconciliation reconciliation =
                new PaymentReconciliation(
                        UUID.randomUUID(),
                        payment,
                        gatewayPayment.gatewayPaymentId(),
                        payment.getAmount(),
                        gatewayPayment.amount(),
                        payment.getStatus().name(),
                        gatewayPayment.status(),
                        reconciliationStatus,
                        LocalDateTime.now()
                );

        return reconciliationRepository.save(
                reconciliation
        );
    }


    private ReconciliationStatus determineStatus(
            Payment payment,
            GatewayPaymentDetails gatewayPayment
    ) {

        if (payment.getAmount()
                .compareTo(gatewayPayment.amount()) != 0) {

            return ReconciliationStatus.AMOUNT_MISMATCH;
        }

        if (!"captured".equalsIgnoreCase(
                gatewayPayment.status()
        )) {

            return ReconciliationStatus.STATUS_MISMATCH;
        }

        if (!payment.getGatewayOrderId()
                .equals(gatewayPayment.gatewayOrderId())) {

            return ReconciliationStatus.ORDER_MISMATCH;
        }

        return ReconciliationStatus.MATCHED;
    }

    public boolean isEligibleForScheduledReconciliation(
            Payment payment
    ) {

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return false;
        }

        if (payment.getGatewayPaymentId() == null) {
            return false;
        }

        boolean alreadyMatched =
                reconciliationRepository
                        .existsByPaymentPaymentIdAndReconciliationStatus(
                                payment.getPaymentId(),
                                ReconciliationStatus.MATCHED
                        );

        return !alreadyMatched;
    }

    public void reconcileEligiblePayments() {

        List<Payment> successfulPayments =
                paymentRepository.findAllByStatus(
                        PaymentStatus.SUCCESS
                );

        for (Payment payment : successfulPayments) {

            if (!isEligibleForScheduledReconciliation(payment)) {
                continue;
            }

            try {
                PaymentReconciliation reconciliation =
                        reconcilePayment(payment.getPaymentId());

                logger.info(
                        "Payment {} reconciled with status {}",
                        payment.getPaymentId(),
                        reconciliation.getReconciliationStatus()

                );

            } catch (Exception ex) {

                logger.error(
                        "Reconciliation failed for payment {}: {}",
                        payment.getPaymentId(),
                        ex.getMessage()
                );

            }
        }
    }



    public ReconciliationSummaryResponse getSummary() {

        List<PaymentReconciliation> reconciliations =
                reconciliationRepository.findAllByOrderByReconciledAtDesc();

        Map<UUID, ReconciliationStatus> latestStatusByPayment =
                new HashMap<>();

        for (PaymentReconciliation reconciliation : reconciliations) {

            UUID paymentId =
                    reconciliation.getPayment().getPaymentId();

            latestStatusByPayment.putIfAbsent(
                    paymentId,
                    reconciliation.getReconciliationStatus()
            );
        }

        long matched = 0;
        long amountMismatch = 0;
        long statusMismatch = 0;
        long orderMismatch = 0;
        long paymentNotFound = 0;

        for (ReconciliationStatus status :
                latestStatusByPayment.values()) {

            switch (status) {

                case MATCHED ->
                        matched++;

                case AMOUNT_MISMATCH ->
                        amountMismatch++;

                case STATUS_MISMATCH ->
                        statusMismatch++;

                case ORDER_MISMATCH ->
                        orderMismatch++;

                case PAYMENT_NOT_FOUND ->
                        paymentNotFound++;
            }
        }

        return new ReconciliationSummaryResponse(
                matched,
                amountMismatch,
                statusMismatch,
                orderMismatch,
                paymentNotFound
        );
    }


    public List<ReconciliationExceptionResponse> getCurrentExceptions() {

        List<PaymentReconciliation> reconciliations =
                reconciliationRepository.findAllByOrderByReconciledAtDesc();

        Map<UUID, PaymentReconciliation> latestByPayment =
                new LinkedHashMap<>();

        for (PaymentReconciliation reconciliation : reconciliations) {

            UUID paymentId =
                    reconciliation.getPayment().getPaymentId();

            latestByPayment.putIfAbsent(
                    paymentId,
                    reconciliation
            );
        }

        return latestByPayment.values()
                .stream()
                .filter(reconciliation ->
                        reconciliation.getReconciliationStatus()
                                != ReconciliationStatus.MATCHED
                )
                .map(reconciliation ->
                        new ReconciliationExceptionResponse(
                                reconciliation.getReconciliationId(),
                                reconciliation.getPayment().getPaymentId(),
                                reconciliation.getGatewayPaymentId(),
                                reconciliation.getInternalAmount(),
                                reconciliation.getGatewayAmount(),
                                reconciliation.getInternalStatus(),
                                reconciliation.getGatewayStatus(),
                                reconciliation.getReconciliationStatus(),
                                reconciliation.getReconciledAt()
                        )
                )
                .toList();
    }



    public List<PaymentReconciliationResponse> getPaymentHistory(
            UUID paymentId
    ) {

        return reconciliationRepository
                .findAllByPaymentPaymentIdOrderByReconciledAtDesc(paymentId)
                .stream()
                .map(reconciliation ->
                        new PaymentReconciliationResponse(
                                reconciliation.getReconciliationId(),
                                reconciliation.getPayment().getPaymentId(),
                                reconciliation.getGatewayPaymentId(),
                                reconciliation.getInternalAmount(),
                                reconciliation.getGatewayAmount(),
                                reconciliation.getInternalStatus(),
                                reconciliation.getGatewayStatus(),
                                reconciliation.getReconciliationStatus(),
                                reconciliation.getReconciledAt()
                        )
                )
                .toList();
    }
}