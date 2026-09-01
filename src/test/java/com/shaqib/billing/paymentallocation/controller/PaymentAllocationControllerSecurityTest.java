package com.shaqib.billing.paymentallocation.controller;

import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.paymentallocation.dto.CreatePaymentAllocationRequest;
import com.shaqib.billing.paymentallocation.entity.PaymentAllocation;
import com.shaqib.billing.paymentallocation.service.PaymentAllocationService;
import com.shaqib.billing.security.auth.CustomUserDetailsService;
import com.shaqib.billing.security.authorization.AccountAuthorizationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentAllocationController.class)
@Import(PaymentAllocationControllerSecurityTest.MethodSecurityConfig.class)
class PaymentAllocationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentAllocationController paymentAllocationController;

    @MockitoBean
    private PaymentAllocationService paymentAllocationService;

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
    void customerCanViewOwnPaymentAllocations() throws Exception {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(true);

        when(paymentAllocationService.getAllocationsByPayment(
                accountId,
                paymentId
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}/payments/{paymentId}/allocations",
                                accountId,
                                paymentId
                        )
                )
                .andExpect(status().isOk());

        verify(paymentAllocationService)
                .getAllocationsByPayment(
                        accountId,
                        paymentId
                );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotViewOtherAccountPaymentAllocations() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(false);

        assertThrows(
                AuthorizationDeniedException.class,
                () -> paymentAllocationController
                        .getAllocationsByPayment(
                                accountId,
                                paymentId
                        )
        );

        verifyNoInteractions(paymentAllocationService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotCreateAllocation() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        CreatePaymentAllocationRequest request =
                new CreatePaymentAllocationRequest(
                        billId,
                        new BigDecimal("500.00")
                );

        assertThrows(
                AuthorizationDeniedException.class,
                () -> paymentAllocationController.createAllocation(
                        accountId,
                        paymentId,
                        request
                )
        );

        verifyNoInteractions(paymentAllocationService);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanCreateAllocation() {

        UUID accountId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        CreatePaymentAllocationRequest request =
                new CreatePaymentAllocationRequest(
                        billId,
                        new BigDecimal("500.00")
                );

        PaymentAllocation allocation =
                mock(PaymentAllocation.class);

        Payment payment = mock(Payment.class);
        Bill bill = mock(Bill.class);

        when(allocation.getPayment()).thenReturn(payment);
        when(allocation.getBill()).thenReturn(bill);

        when(payment.getPaymentId()).thenReturn(paymentId);
        when(bill.getBillId()).thenReturn(billId);

        when(paymentAllocationService.createAllocation(
                accountId,
                paymentId,
                billId,
                new BigDecimal("500.00")
        )).thenReturn(allocation);

        paymentAllocationController.createAllocation(
                accountId,
                paymentId,
                request
        );

        verify(paymentAllocationService)
                .createAllocation(
                        accountId,
                        paymentId,
                        billId,
                        new BigDecimal("500.00")
                );
    }
}