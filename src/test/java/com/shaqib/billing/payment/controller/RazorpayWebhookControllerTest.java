package com.shaqib.billing.payment.controller;

import com.shaqib.billing.payment.gateway.PaymentGateway;
import com.shaqib.billing.payment.service.PaymentService;
import com.shaqib.billing.security.auth.CustomUserDetailsService;
import com.shaqib.billing.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RazorpayWebhookController.class)
class RazorpayWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void invalidSignatureReturnsBadRequest() throws Exception {

        String payload = """
                {
                  "event": "payment.captured"
                }
                """;

        when(paymentGateway.verifyWebhook(
                payload,
                "invalid-signature"
        )).thenReturn(false);

        mockMvc.perform(
                        post("/api/v1/payments/webhooks/razorpay")
                                .header(
                                        "X-Razorpay-Signature",
                                        "invalid-signature"
                                )
                                .contentType("application/json")
                                .content(payload)
                )
                .andExpect(status().isBadRequest());

        verify(paymentGateway).verifyWebhook(
                payload,
                "invalid-signature"
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    void validNonCapturedEventReturnsOkWithoutProcessingPayment()
            throws Exception {

        String payload = """
                {
                  "event": "payment.failed"
                }
                """;

        when(paymentGateway.verifyWebhook(
                payload,
                "valid-signature"
        )).thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/payments/webhooks/razorpay")
                                .header(
                                        "X-Razorpay-Signature",
                                        "valid-signature"
                                )
                                .contentType("application/json")
                                .content(payload)
                )
                .andExpect(status().isOk());

        verify(paymentGateway).verifyWebhook(
                payload,
                "valid-signature"
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    void validCapturedPaymentProcessesWebhook() throws Exception {

        String payload = """
                {
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_123",
                        "order_id": "order_123"
                      }
                    }
                  }
                }
                """;

        when(paymentGateway.verifyWebhook(
                payload,
                "valid-signature"
        )).thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/payments/webhooks/razorpay")
                                .header(
                                        "X-Razorpay-Signature",
                                        "valid-signature"
                                )
                                .contentType("application/json")
                                .content(payload)
                )
                .andExpect(status().isOk());

        verify(paymentGateway).verifyWebhook(
                payload,
                "valid-signature"
        );

        verify(paymentService)
                .processCapturedPaymentWebhook(
                        "order_123",
                        "pay_123"
                );
    }
}