package com.shaqib.billing.reconciliation.controller;

import com.shaqib.billing.reconciliation.dto.PaymentReconciliationResponse;
import com.shaqib.billing.reconciliation.dto.ReconciliationExceptionResponse;
import com.shaqib.billing.reconciliation.dto.ReconciliationSummaryResponse;
import com.shaqib.billing.reconciliation.entity.PaymentReconciliation;
import com.shaqib.billing.reconciliation.service.PaymentReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentReconciliationController {

    private final PaymentReconciliationService reconciliationService;

    public PaymentReconciliationController(
            PaymentReconciliationService reconciliationService
    ) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/{paymentId}/reconcile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentReconciliationResponse> reconcilePayment(
            @PathVariable UUID paymentId
    ) {

        PaymentReconciliation reconciliation =
                reconciliationService.reconcilePayment(paymentId);

        PaymentReconciliationResponse response =
                new PaymentReconciliationResponse(
                        reconciliation.getReconciliationId(),
                        reconciliation.getPayment().getPaymentId(),
                        reconciliation.getGatewayPaymentId(),
                        reconciliation.getInternalAmount(),
                        reconciliation.getGatewayAmount(),
                        reconciliation.getInternalStatus(),
                        reconciliation.getGatewayStatus(),
                        reconciliation.getReconciliationStatus(),
                        reconciliation.getReconciledAt()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reconciliation/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReconciliationSummaryResponse> getReconciliationSummary() {

        return ResponseEntity.ok(
                reconciliationService.getSummary()
        );
    }

    @GetMapping("/reconciliation/exceptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReconciliationExceptionResponse>>
    getReconciliationExceptions() {

        return ResponseEntity.ok(
                reconciliationService.getCurrentExceptions()
        );
    }


    @GetMapping("/{paymentId}/reconciliation/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentReconciliationResponse>>
    getPaymentReconciliationHistory(
            @PathVariable UUID paymentId
    ) {

        return ResponseEntity.ok(
                reconciliationService.getPaymentHistory(paymentId)
        );
    }

}