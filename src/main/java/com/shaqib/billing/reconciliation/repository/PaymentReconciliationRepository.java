package com.shaqib.billing.reconciliation.repository;

import com.shaqib.billing.reconciliation.entity.PaymentReconciliation;
import com.shaqib.billing.reconciliation.entity.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentReconciliationRepository
        extends JpaRepository<PaymentReconciliation, UUID> {

    List<PaymentReconciliation> findAllByPaymentPaymentId(UUID paymentId);

    boolean existsByPaymentPaymentIdAndReconciliationStatus(
            UUID paymentId,
            ReconciliationStatus reconciliationStatus
    );

    long countByReconciliationStatus(
            ReconciliationStatus reconciliationStatus
    );
    List<PaymentReconciliation> findAllByOrderByReconciledAtDesc();

    List<PaymentReconciliation> findAllByPaymentPaymentIdOrderByReconciledAtDesc(
            UUID paymentId
    );

}