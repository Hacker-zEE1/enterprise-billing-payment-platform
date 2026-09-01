package com.shaqib.billing.paymentallocation.controller;

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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BillPaymentAllocationController.class)
@Import(BillPaymentAllocationControllerSecurityTest.MethodSecurityConfig.class)
class BillPaymentAllocationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillPaymentAllocationController billPaymentAllocationController;

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
    void customerCanViewOwnBillAllocations() throws Exception {

        UUID accountId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(true);

        when(paymentAllocationService.getAllocationsByBill(
                accountId,
                billId
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}/bills/{billId}/allocations",
                                accountId,
                                billId
                        )
                )
                .andExpect(status().isOk());

        verify(paymentAllocationService)
                .getAllocationsByBill(
                        accountId,
                        billId
                );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotViewOtherAccountBillAllocations() {

        UUID accountId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(false);

        assertThrows(
                AuthorizationDeniedException.class,
                () -> billPaymentAllocationController
                        .getAllocationsByBill(
                                accountId,
                                billId
                        )
        );

        verifyNoInteractions(paymentAllocationService);
    }
}