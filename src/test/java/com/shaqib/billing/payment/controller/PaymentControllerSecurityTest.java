package com.shaqib.billing.payment.controller;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.payment.dto.CreatePaymentRequest;
import com.shaqib.billing.payment.dto.VerifyPaymentRequest;
import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.entity.PaymentMethod;
import com.shaqib.billing.payment.service.PaymentService;
import com.shaqib.billing.security.authorization.AccountAuthorizationService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import(PaymentControllerSecurityTest.MethodSecurityConfig.class)
class PaymentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentController paymentController;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean(name = "accountAuthorizationService")
    private AccountAuthorizationService accountAuthorizationService;

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
    void customerCanViewPaymentsForOwnAccount() throws Exception {

        UUID accountId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(true);

        when(paymentService.getPaymentsByAccountId(accountId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/accounts/{accountId}/payments", accountId)
                )
                .andExpect(status().isOk());

        verify(paymentService)
                .getPaymentsByAccountId(accountId);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotViewPaymentsForAnotherAccount() {

        UUID accountId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(false);

        assertThrows(
                AuthorizationDeniedException.class,
                () -> paymentController
                        .getPaymentsByAccountId(accountId)
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotMarkPaymentSuccess() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        assertThrows(
                AuthorizationDeniedException.class,
                () -> paymentController.markPaymentSuccess(
                        accountId,
                        paymentId
                )
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanMarkPaymentSuccess() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Payment payment = mock(Payment.class);
        Account account = mock(Account.class);

        when(payment.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(accountId);

        when(paymentService.markPaymentSuccess(
                accountId,
                paymentId
        )).thenReturn(payment);

        paymentController.markPaymentSuccess(
                accountId,
                paymentId
        );

        verify(paymentService)
                .markPaymentSuccess(
                        accountId,
                        paymentId
                );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCanCreatePaymentForOwnAccount() {

        UUID accountId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(true);

        Payment payment = mock(Payment.class);
        Account account = mock(Account.class);

        when(payment.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(accountId);

        when(paymentService.createPayment(
                eq(accountId),
                eq(billId),
                any(),
                any()
        )).thenReturn(payment);

        paymentController.createPayment(
                accountId,
                new CreatePaymentRequest(
                        new BigDecimal("500.00"),
                        PaymentMethod.UPI,
                        billId
                )
        );

        verify(paymentService).createPayment(
                eq(accountId),
                eq(billId),
                eq(new BigDecimal("500.00")),
                eq(PaymentMethod.UPI)
        );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotCreatePaymentForAnotherAccount() {

        UUID accountId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(false);

        assertThrows(
                AuthorizationDeniedException.class,
                () -> paymentController.createPayment(
                        accountId,
                        new CreatePaymentRequest(
                                new BigDecimal("500.00"),
                                PaymentMethod.UPI,
                                billId
                        )
                )
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCanVerifyPaymentForOwnAccount() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(true);

        Payment payment = mock(Payment.class);
        Account account = mock(Account.class);

        when(payment.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(accountId);

        when(paymentService.verifyPayment(
                accountId,
                paymentId,
                "order_123",
                "pay_123",
                "signature_123"
        )).thenReturn(payment);

        paymentController.verifyPayment(
                accountId,
                paymentId,
                new VerifyPaymentRequest(
                        "order_123",
                        "pay_123",
                        "signature_123"
                )
        );

        verify(paymentService).verifyPayment(
                accountId,
                paymentId,
                "order_123",
                "pay_123",
                "signature_123"
        );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotCancelPayment() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        assertThrows(
                AuthorizationDeniedException.class,
                () -> paymentController.cancelPayment(
                        accountId,
                        paymentId
                )
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanCancelPayment() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Payment payment = mock(Payment.class);
        Account account = mock(Account.class);

        when(payment.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(accountId);

        when(paymentService.cancelPayment(
                accountId,
                paymentId
        )).thenReturn(payment);

        paymentController.cancelPayment(
                accountId,
                paymentId
        );

        verify(paymentService).cancelPayment(
                accountId,
                paymentId
        );
    }
}