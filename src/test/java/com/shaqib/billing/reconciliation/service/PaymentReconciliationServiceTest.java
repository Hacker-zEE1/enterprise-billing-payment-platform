package com.shaqib.billing.reconciliation.service;

import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.entity.PaymentStatus;
import com.shaqib.billing.payment.gateway.GatewayPaymentDetails;
import com.shaqib.billing.payment.gateway.PaymentGateway;
import com.shaqib.billing.payment.repository.PaymentRepository;
import com.shaqib.billing.reconciliation.entity.PaymentReconciliation;
import com.shaqib.billing.reconciliation.entity.ReconciliationStatus;
import com.shaqib.billing.reconciliation.repository.PaymentReconciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentReconciliationServiceTest {

    private PaymentRepository paymentRepository;
    private PaymentGateway paymentGateway;
    private PaymentReconciliationRepository reconciliationRepository;

    private PaymentReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {

        paymentRepository =
                Mockito.mock(PaymentRepository.class);

        paymentGateway =
                Mockito.mock(PaymentGateway.class);

        reconciliationRepository =
                Mockito.mock(PaymentReconciliationRepository.class);

        reconciliationService =
                new PaymentReconciliationService(
                        paymentRepository,
                        paymentGateway,
                        reconciliationRepository
                );
    }

    @Test
    void shouldReturnStatusMismatchWhenGatewayPaymentIsNotCaptured() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = Mockito.mock(Payment.class);

        when(payment.getPaymentId())
                .thenReturn(paymentId);

        when(payment.getAmount())
                .thenReturn(new BigDecimal("20.00"));

        when(payment.getStatus())
                .thenReturn(PaymentStatus.SUCCESS);

        when(payment.getGatewayPaymentId())
                .thenReturn("pay_test");

        when(payment.getGatewayOrderId())
                .thenReturn("order_test");

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        GatewayPaymentDetails gatewayPayment =
                new GatewayPaymentDetails(
                        "pay_test",
                        "order_test",
                        new BigDecimal("20.00"),
                        "failed"
                );

        when(paymentGateway.fetchPayment("pay_test"))
                .thenReturn(gatewayPayment);

        when(reconciliationRepository.save(any(PaymentReconciliation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentReconciliation result =
                reconciliationService.reconcilePayment(paymentId);

        assertEquals(
                ReconciliationStatus.STATUS_MISMATCH,
                result.getReconciliationStatus()
        );
    }
}