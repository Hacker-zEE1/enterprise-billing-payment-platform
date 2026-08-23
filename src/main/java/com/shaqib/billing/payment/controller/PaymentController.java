package com.shaqib.billing.payment.controller;

import com.shaqib.billing.payment.dto.CreatePaymentRequest;
import com.shaqib.billing.payment.dto.PaymentResponse;
import com.shaqib.billing.payment.dto.VerifyPaymentRequest;
import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable UUID accountId,
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        Payment payment = paymentService.createPayment(
                accountId,
                request.billId(),
                request.amount(),
                request.paymentMethod()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getAccount().getAccountId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getGateway(),
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId(),
                payment.getPaymentDate(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId
    ) {

        Payment payment = paymentService.getPaymentById(
                accountId,
                paymentId
        );

        return ResponseEntity.ok(toResponse(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPaymentsByAccountId(
            @PathVariable UUID accountId
    ) {

        List<PaymentResponse> response = paymentService
                .getPaymentsByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse> markPaymentSuccess(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId
    ) {

        Payment payment = paymentService.markPaymentSuccess(
                accountId,
                paymentId
        );

        return ResponseEntity.ok(toResponse(payment));
    }

    @PatchMapping("/{paymentId}/failed")
    public ResponseEntity<PaymentResponse> markPaymentFailed(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId
    ) {

        Payment payment = paymentService.markPaymentFailed(
                accountId,
                paymentId
        );

        return ResponseEntity.ok(toResponse(payment));
    }

    @PatchMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId
    ) {

        Payment payment = paymentService.cancelPayment(
                accountId,
                paymentId
        );

        return ResponseEntity.ok(toResponse(payment));
    }

    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId,
            @Valid @RequestBody VerifyPaymentRequest request
    ) {

        Payment payment = paymentService.verifyPayment(
                accountId,
                paymentId,
                request.gatewayOrderId(),
                request.gatewayPaymentId(),
                request.signature()
        );

        return ResponseEntity.ok(toResponse(payment));
    }

}