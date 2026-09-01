package com.shaqib.billing.bill.controller;

import com.shaqib.billing.bill.dto.BillResponse;
import com.shaqib.billing.bill.dto.CreateBillRequest;
import com.shaqib.billing.bill.dto.PayableBillResponse;
import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.bill.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillResponse> createBill(
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateBillRequest request
    ) {

        Bill bill = billService.createBill(
                accountId,
                request.billingPeriodStart(),
                request.billingPeriodEnd(),
                request.dueDate(),
                request.totalAmount()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(bill));
    }

    @GetMapping("/payable")
    @PreAuthorize(
            "@accountAuthorizationService.canAccessAccount(authentication, #accountId)"
    )
    public ResponseEntity<List<PayableBillResponse>> getPayableBills(
            @PathVariable UUID accountId
    ) {
        return ResponseEntity.ok(
                billService.getPayableBills(accountId)
        );
    }


    @GetMapping("/{billId}")
    @PreAuthorize(
            "@accountAuthorizationService.canAccessAccount(authentication, #accountId)"
    )
    public ResponseEntity<BillResponse> getBillById(
            @PathVariable UUID accountId,
            @PathVariable UUID billId
    ) {

        Bill bill = billService.getBillById(accountId, billId);

        return ResponseEntity.ok(toResponse(bill));
    }

    @GetMapping
    @PreAuthorize(
            "@accountAuthorizationService.canAccessAccount(authentication, #accountId)"
    )

    public ResponseEntity<List<BillResponse>> getBillsByAccountId(
            @PathVariable UUID accountId
    ) {

        List<BillResponse> response = billService
                .getBillsByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{billId}/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillResponse> issueBill(
            @PathVariable UUID accountId,
            @PathVariable UUID billId
    ) {

        Bill bill = billService.issueBill(accountId, billId);

        return ResponseEntity.ok(toResponse(bill));
    }

    @PatchMapping("/{billId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillResponse> cancelBill(
            @PathVariable UUID accountId,
            @PathVariable UUID billId
    ) {

        Bill bill = billService.cancelBill(accountId, billId);

        return ResponseEntity.ok(toResponse(bill));
    }


    private BillResponse toResponse(Bill bill) {
        return new BillResponse(
                bill.getBillId(),
                bill.getAccount().getAccountId(),
                bill.getBillNumber(),
                bill.getBillingPeriodStart(),
                bill.getBillingPeriodEnd(),
                bill.getDueDate(),
                bill.getTotalAmount(),
                bill.getStatus(),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }
}