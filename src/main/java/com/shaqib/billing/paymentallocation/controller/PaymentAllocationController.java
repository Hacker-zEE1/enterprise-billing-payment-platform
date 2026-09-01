package com.shaqib.billing.paymentallocation.controller;

import com.shaqib.billing.paymentallocation.dto.CreatePaymentAllocationRequest;
import com.shaqib.billing.paymentallocation.dto.PaymentAllocationResponse;
import com.shaqib.billing.paymentallocation.entity.PaymentAllocation;
import com.shaqib.billing.paymentallocation.service.PaymentAllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/payments/{paymentId}/allocations")
public class PaymentAllocationController {

    private final PaymentAllocationService paymentAllocationService;

    public PaymentAllocationController(
            PaymentAllocationService paymentAllocationService
    ) {
        this.paymentAllocationService = paymentAllocationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentAllocationResponse> createAllocation(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId,
            @Valid @RequestBody CreatePaymentAllocationRequest request
    ) {

        PaymentAllocation allocation =
                paymentAllocationService.createAllocation(
                        accountId,
                        paymentId,
                        request.billId(),
                        request.allocatedAmount()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(allocation));
    }

    private PaymentAllocationResponse toResponse(
            PaymentAllocation allocation
    ) {
        return new PaymentAllocationResponse(
                allocation.getAllocationId(),
                allocation.getPayment().getPaymentId(),
                allocation.getBill().getBillId(),
                allocation.getAllocatedAmount(),
                allocation.getCreatedAt()
        );
    }


    @GetMapping
    @PreAuthorize(
            "@accountAuthorizationService.canAccessAccount(authentication, #accountId)"
    )
    public ResponseEntity<List<PaymentAllocationResponse>> getAllocationsByPayment(
            @PathVariable UUID accountId,
            @PathVariable UUID paymentId
    ) {

        List<PaymentAllocationResponse> response =
                paymentAllocationService
                        .getAllocationsByPayment(accountId, paymentId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }
}