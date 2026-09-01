package com.shaqib.billing.bill.controller;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.bill.dto.CreateBillRequest;
import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.bill.service.BillService;
import com.shaqib.billing.security.authorization.AccountAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.shaqib.billing.security.jwt.JwtService;
import com.shaqib.billing.security.auth.CustomUserDetailsService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillController.class)
@Import(BillControllerSecurityTest.MethodSecurityConfig.class)
class BillControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BillController billController;

    @MockitoBean
    private BillService billService;

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
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void customerCanViewBillsForOwnAccount() throws Exception {

        UUID accountId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(true);

        when(billService.getBillsByAccountId(accountId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/accounts/{accountId}/bills", accountId)
                )
                .andExpect(status().isOk());

        verify(billService).getBillsByAccountId(accountId);
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void customerCannotViewBillsForAnotherAccount() {

        UUID accountId = UUID.randomUUID();

        when(accountAuthorizationService.canAccessAccount(
                any(),
                eq(accountId)
        )).thenReturn(false);

        assertThrows(
                AuthorizationDeniedException.class,
                () -> billController.getBillsByAccountId(accountId)
        );

        verifyNoInteractions(billService);
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void customerCannotCreateBill() {

        UUID accountId = UUID.randomUUID();

        assertThrows(
                AuthorizationDeniedException.class,
                () -> billController.createBill(
                        accountId,
                        new CreateBillRequest(
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31),
                                LocalDate.of(2026, 9, 15),
                                new BigDecimal("100")
                        )
                )
        );

        verifyNoInteractions(billService);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminCanCreateBill() throws Exception {

        UUID accountId = UUID.randomUUID();

        Bill bill = mock(Bill.class);
        Account account = mock(Account.class);

        when(bill.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(accountId);

        when(billService.createBill(
                eq(accountId),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(bill);

        mockMvc.perform(
                        post("/api/v1/accounts/{accountId}/bills", accountId)
                                .contentType("application/json")
                                .content("""
                                        {
                                          "billingPeriodStart": "2026-08-01",
                                          "billingPeriodEnd": "2026-08-31",
                                          "dueDate": "2026-09-15",
                                          "totalAmount": 100
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        verify(billService).createBill(
                eq(accountId),
                any(),
                any(),
                any(),
                any()
        );
    }
}