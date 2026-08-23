package com.shaqib.billing.payment.controller;

import com.shaqib.billing.payment.gateway.PaymentGateway;
import com.shaqib.billing.payment.service.PaymentService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
public class RazorpayWebhookController {

    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;

    public RazorpayWebhookController(
            PaymentGateway paymentGateway,
            PaymentService paymentService
    ) {
        this.paymentGateway = paymentGateway;
        this.paymentService = paymentService;
    }

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {

        boolean valid = paymentGateway.verifyWebhook(
                payload,
                signature
        );

        if (!valid) {
            return ResponseEntity.badRequest().build();
        }

        JSONObject webhookPayload = new JSONObject(payload);

        String event = webhookPayload.getString("event");

        if ("payment.captured".equals(event)) {

            JSONObject paymentEntity = webhookPayload
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String gatewayPaymentId =
                    paymentEntity.getString("id");

            String gatewayOrderId =
                    paymentEntity.getString("order_id");

            paymentService.processCapturedPaymentWebhook(
                    gatewayOrderId,
                    gatewayPaymentId
            );

            System.out.println(
                    "Captured Razorpay payment. Order ID: "
                            + gatewayOrderId
                            + ", Payment ID: "
                            + gatewayPaymentId
            );
        }

        return ResponseEntity.ok().build();
    }
}