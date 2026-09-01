package com.shaqib.billing.security.jwt;

import com.shaqib.billing.security.auth.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        userDetailsService
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithoutAuthorizationHeaderContinuesWithoutAuthentication()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
    }

    @Test
    void malformedTokenReturnsUnauthorized()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(jwtService.extractEmail("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain, never())
                .doFilter(request, response);
    }

    @Test
    void validTokenCreatesAuthenticationAndContinues()
            throws Exception {

        String token = "valid-token";
        String email = "customer@example.com";

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        UserDetails userDetails =
                User.withUsername(email)
                        .password("password")
                        .roles("CUSTOMER")
                        .build();

        when(jwtService.extractEmail(token))
                .thenReturn(email);

        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid(token, userDetails))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        assertEquals(
                email,
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        assertTrue(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .isAuthenticated()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void tokenForUnknownUserReturnsUnauthorized()
            throws Exception {

        String token = "valid-looking-token";
        String email = "deleted@example.com";

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(jwtService.extractEmail(token))
                .thenReturn(email);

        when(userDetailsService.loadUserByUsername(email))
                .thenThrow(
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain, never())
                .doFilter(request, response);
    }
}