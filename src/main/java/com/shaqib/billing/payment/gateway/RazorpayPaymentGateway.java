package com.shaqib.billing.payment.gateway;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.shaqib.billing.payment.entity.PaymentGatewayProvider;
import com.shaqib.billing.payment.exception.GatewayPaymentNotFoundException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RazorpayPaymentGateway implements PaymentGateway {

    private final RazorpayProperties razorpayProperties;

    public RazorpayPaymentGateway(RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    @Override
    public GatewayOrderResponse createOrder(
            BigDecimal amount,
            String currency,
            String receipt
    ) {

        try {
            RazorpayClient razorpayClient = new RazorpayClient(
                    razorpayProperties.getKeyId(),
                    razorpayProperties.getKeySecret()
            );

            long amountInPaise = amount
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);

            Order order = razorpayClient.orders.create(orderRequest);

            return new GatewayOrderResponse(
                    order.get("id"),
                    PaymentGatewayProvider.RAZORPAY,
                    order.get("status")
            );

        } catch (RazorpayException e) {
            throw new PaymentGatewayException(
                    "Failed to create Razorpay order",
                    e
            );
        }
    }

    @Override
    public boolean verifyPayment(
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    ) {

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", gatewayOrderId);
            attributes.put("razorpay_payment_id", gatewayPaymentId);
            attributes.put("razorpay_signature", signature);

            return com.razorpay.Utils.verifyPaymentSignature(
                    attributes,
                    razorpayProperties.getKeySecret()
            );

        } catch (RazorpayException e) {
            throw new PaymentGatewayException(
                    "Failed to verify Razorpay payment signature",
                    e
            );
        }
    }


    @Override
    public boolean verifyWebhook(
            String payload,
            String signature
    ) {
        try {
            return com.razorpay.Utils.verifyWebhookSignature(
                    payload,
                    signature,
                    razorpayProperties.getWebhookSecret()
            );
        } catch (RazorpayException e) {
            throw new PaymentGatewayException(
                    "Failed to verify Razorpay webhook signature",
                    e
            );
        }
    }

    @Override
    public GatewayPaymentDetails fetchPayment(
            String gatewayPaymentId
    ) {

        try {

            RazorpayClient client =
                    new RazorpayClient(
                            razorpayProperties.getKeyId(),
                            razorpayProperties.getKeySecret()
                    );

            com.razorpay.Payment razorpayPayment =
                    client.payments.fetch(gatewayPaymentId);

            Number amountValue =
                    razorpayPayment.get("amount");

            BigDecimal amount =
                    BigDecimal.valueOf(amountValue.longValue())
                            .divide(BigDecimal.valueOf(100));

            String orderId =
                    razorpayPayment.get("order_id");

            String status =
                    razorpayPayment.get("status");

            return new GatewayPaymentDetails(
                    gatewayPaymentId,
                    orderId,
                    amount,
                    status
            );

        } catch (com.razorpay.RazorpayException ex) {

            if (ex.getStatusCode() == 400
                    && "BAD_REQUEST_ERROR".equalsIgnoreCase(ex.getCode())
                    && "input_validation_failed".equalsIgnoreCase(ex.getReason())
                    && ex.getDescription() != null
                    && ex.getDescription()
                    .toLowerCase()
                    .contains("does not exist")) {

                throw new GatewayPaymentNotFoundException(
                        "Razorpay payment not found: " + gatewayPaymentId
                );
            }

            throw new PaymentGatewayException(
                    "Failed to fetch Razorpay payment"
            );
        }
    }
}