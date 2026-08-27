package com.shaqib.billing.reconciliation.scheduler;

import com.shaqib.billing.reconciliation.service.PaymentReconciliationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentReconciliationScheduler {

    private final PaymentReconciliationService reconciliationService;

    public PaymentReconciliationScheduler(
            PaymentReconciliationService reconciliationService
    ) {
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void reconcilePayments() {

        reconciliationService.reconcileEligiblePayments();
    }
}