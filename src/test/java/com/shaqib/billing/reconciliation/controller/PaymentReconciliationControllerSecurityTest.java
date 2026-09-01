package com.shaqib.billing.reconciliation.controller;

import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.reconciliation.entity.PaymentReconciliation;
import com.shaqib.billing.reconciliation.service.PaymentReconciliationService;
import com.shaqib.billing.security.auth.CustomUserDetailsService;
import com.shaqib.billing.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = PaymentReconciliationController.class)
@Import(PaymentReconciliationControllerSecurityTest.MethodSecurityConfig.class)
class PaymentReconciliationControllerSecurityTest {

    @Autowired
    private PaymentReconciliationController reconciliationController;

    @MockitoBean
    private PaymentReconciliationService reconciliationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotReconcilePayment() {

        UUID paymentId = UUID.randomUUID();

        assertThrows(
                AuthorizationDeniedException.class,
                () -> reconciliationController.reconcilePayment(paymentId)
        );

        verifyNoInteractions(reconciliationService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotViewReconciliationSummary() {

        assertThrows(
                AuthorizationDeniedException.class,
                () -> reconciliationController.getReconciliationSummary()
        );

        verifyNoInteractions(reconciliationService);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanReconcilePayment() {

        UUID paymentId = UUID.randomUUID();

        PaymentReconciliation reconciliation =
                mock(PaymentReconciliation.class);

        Payment payment = mock(Payment.class);

        when(reconciliation.getPayment())
                .thenReturn(payment);

        when(payment.getPaymentId())
                .thenReturn(paymentId);

        when(reconciliationService.reconcilePayment(paymentId))
                .thenReturn(reconciliation);

        reconciliationController.reconcilePayment(paymentId);

        verify(reconciliationService)
                .reconcilePayment(paymentId);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanViewReconciliationSummary() {

        reconciliationController.getReconciliationSummary();

        verify(reconciliationService)
                .getSummary();
    }
}