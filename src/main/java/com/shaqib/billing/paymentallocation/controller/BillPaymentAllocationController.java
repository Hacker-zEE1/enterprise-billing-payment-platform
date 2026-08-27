package com.shaqib.billing.paymentallocation.controller;

import com.shaqib.billing.paymentallocation.dto.PaymentAllocationResponse;
import com.shaqib.billing.paymentallocation.entity.PaymentAllocation;
import com.shaqib.billing.paymentallocation.service.PaymentAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/bills/{billId}/allocations")
public class BillPaymentAllocationController {

    private final PaymentAllocationService paymentAllocationService;

    public BillPaymentAllocationController(
            PaymentAllocationService paymentAllocationService
    ) {
        this.paymentAllocationService = paymentAllocationService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentAllocationResponse>> getAllocationsByBill(
            @PathVariable UUID accountId,
            @PathVariable UUID billId
    ) {

        List<PaymentAllocationResponse> response =
                paymentAllocationService
                        .getAllocationsByBill(accountId, billId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
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
}