package com.shaqib.billing.reconciliation.controller;

import com.shaqib.billing.reconciliation.dto.PaymentReconciliationResponse;
import com.shaqib.billing.reconciliation.dto.ReconciliationExceptionResponse;
import com.shaqib.billing.reconciliation.dto.ReconciliationSummaryResponse;
import com.shaqib.billing.reconciliation.entity.PaymentReconciliation;
import com.shaqib.billing.reconciliation.service.PaymentReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ReconciliationSummaryResponse> getReconciliationSummary() {

        return ResponseEntity.ok(
                reconciliationService.getSummary()
        );
    }

    @GetMapping("/reconciliation/exceptions")
    public ResponseEntity<List<ReconciliationExceptionResponse>>
    getReconciliationExceptions() {

        return ResponseEntity.ok(
                reconciliationService.getCurrentExceptions()
        );
    }


    @GetMapping("/{paymentId}/reconciliation/history")
    public ResponseEntity<List<PaymentReconciliationResponse>>
    getPaymentReconciliationHistory(
            @PathVariable UUID paymentId
    ) {

        return ResponseEntity.ok(
                reconciliationService.getPaymentHistory(paymentId)
        );
    }

}