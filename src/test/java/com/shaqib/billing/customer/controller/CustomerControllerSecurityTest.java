package com.shaqib.billing.customer.controller;

import com.shaqib.billing.customer.dto.CreateCustomerRequest;
import com.shaqib.billing.customer.dto.UpdateCustomerRequest;
import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.customer.service.CustomerService;
import com.shaqib.billing.security.auth.CustomUserDetailsService;
import com.shaqib.billing.security.authorization.CustomerAuthorizationService;
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

@WebMvcTest(controllers = CustomerController.class)
@Import(CustomerControllerSecurityTest.MethodSecurityConfig.class)
class CustomerControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerController customerController;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean(name = "customerAuthorizationService")
    private CustomerAuthorizationService customerAuthorizationService;

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
    void customerCanViewOwnProfile() throws Exception {

        UUID customerId = UUID.randomUUID();

        when(customerAuthorizationService.canAccessCustomer(
                any(),
                eq(customerId)
        )).thenReturn(true);

        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);

        when(customerService.getCustomerById(customerId))
                .thenReturn(customer);

        mockMvc.perform(
                        get("/api/v1/customers/{customerId}", customerId)
                )
                .andExpect(status().isOk());

        verify(customerService)
                .getCustomerById(customerId);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotViewAnotherCustomer() {

        UUID customerId = UUID.randomUUID();

        when(customerAuthorizationService.canAccessCustomer(
                any(),
                eq(customerId)
        )).thenReturn(false);

        assertThrows(
                AuthorizationDeniedException.class,
                () -> customerController.getCustomerById(customerId)
        );

        verifyNoInteractions(customerService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotViewAllCustomers() {

        assertThrows(
                AuthorizationDeniedException.class,
                () -> customerController.getAllCustomers()
        );

        verifyNoInteractions(customerService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotDeactivateCustomer() {

        UUID customerId = UUID.randomUUID();

        assertThrows(
                AuthorizationDeniedException.class,
                () -> customerController.deactivateCustomer(customerId)
        );

        verifyNoInteractions(customerService);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanViewAllCustomers() {

        when(customerService.getAllCustomers())
                .thenReturn(List.of());

        customerController.getAllCustomers();

        verify(customerService)
                .getAllCustomers();
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanDeactivateCustomer() {

        UUID customerId = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        when(customer.getCustomerId())
                .thenReturn(customerId);

        when(customerService.deactivateCustomer(customerId))
                .thenReturn(customer);

        customerController.deactivateCustomer(customerId);

        verify(customerService)
                .deactivateCustomer(customerId);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCanUpdateOwnProfile() {

        UUID customerId = UUID.randomUUID();

        when(customerAuthorizationService.canAccessCustomer(
                any(),
                eq(customerId)
        )).thenReturn(true);

        UpdateCustomerRequest request =
                new UpdateCustomerRequest(
                        "Updated",
                        "Customer",
                        "9999999999"
                );

        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);

        when(customerService.updateCustomer(
                customerId,
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        )).thenReturn(customer);

        customerController.updateCustomer(
                customerId,
                request
        );

        verify(customerService).updateCustomer(
                customerId,
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotCreateCustomer() {

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "New",
                        "Customer",
                        "new.customer@example.com",
                        "9999999999"
                );

        assertThrows(
                AuthorizationDeniedException.class,
                () -> customerController.createCustomer(request)
        );

        verifyNoInteractions(customerService);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanCreateCustomer() {

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "New",
                        "Customer",
                        "new.customer@example.com",
                        "9999999999"
                );

        Customer customer = mock(Customer.class);

        when(customerService.createCustomer(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber()
        )).thenReturn(customer);

        customerController.createCustomer(request);

        verify(customerService).createCustomer(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber()
        );
    }
}