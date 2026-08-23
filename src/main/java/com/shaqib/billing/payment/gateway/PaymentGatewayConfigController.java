package com.shaqib.billing.payment.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/gateway")
public class PaymentGatewayConfigController {

    private final RazorpayProperties razorpayProperties;

    public PaymentGatewayConfigController(RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    @GetMapping("/config")
    public Map<String, String> getGatewayConfig() {
        return Map.of(
                "keyId", razorpayProperties.getKeyId()
        );
    }
}