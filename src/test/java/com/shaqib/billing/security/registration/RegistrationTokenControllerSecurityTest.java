package com.shaqib.billing.security.registration;

import com.shaqib.billing.security.auth.CustomUserDetailsService;
import com.shaqib.billing.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
@WebMvcTest(RegistrationTokenController.class)
@Import(RegistrationTokenControllerSecurityTest.MethodSecurityConfig.class)
class RegistrationTokenControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationTokenService registrationTokenService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RegistrationTokenController registrationTokenController;

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void customerCannotCreateRegistrationToken() {

        UUID customerId = UUID.randomUUID();

        assertThrows(
                AuthorizationDeniedException.class,
                () -> registrationTokenController
                        .createRegistrationToken(customerId)
        );
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminCanCreateRegistrationToken() throws Exception {

        UUID customerId = UUID.randomUUID();

        when(registrationTokenService.createToken(customerId))
                .thenReturn("generated-registration-token");

        mockMvc.perform(
                        post(
                                "/api/v1/customers/{customerId}/registration-token",
                                customerId
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.registrationToken")
                                .value("generated-registration-token")
                );
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }
}