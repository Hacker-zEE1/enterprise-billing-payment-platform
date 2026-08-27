package com.shaqib.billing.reconciliation.entity;

import com.shaqib.billing.payment.entity.Payment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_reconciliations")
public class PaymentReconciliation {

    @Id
    @Column(name = "reconciliation_id", nullable = false)
    private UUID reconciliationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Column(name = "internal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal internalAmount;

    @Column(name = "gateway_amount", precision = 18, scale = 2)
    private BigDecimal gatewayAmount;

    @Column(name = "internal_status", nullable = false, length = 30)
    private String internalStatus;

    @Column(name = "gateway_status", length = 30)
    private String gatewayStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", nullable = false, length = 30)
    private ReconciliationStatus reconciliationStatus;

    @Column(name = "reconciled_at", nullable = false)
    private LocalDateTime reconciledAt;

    protected PaymentReconciliation() {
    }

    public PaymentReconciliation(
            UUID reconciliationId,
            Payment payment,
            String gatewayPaymentId,
            BigDecimal internalAmount,
            BigDecimal gatewayAmount,
            String internalStatus,
            String gatewayStatus,
            ReconciliationStatus reconciliationStatus,
            LocalDateTime reconciledAt
    ) {
        this.reconciliationId = reconciliationId;
        this.payment = payment;
        this.gatewayPaymentId = gatewayPaymentId;
        this.internalAmount = internalAmount;
        this.gatewayAmount = gatewayAmount;
        this.internalStatus = internalStatus;
        this.gatewayStatus = gatewayStatus;
        this.reconciliationStatus = reconciliationStatus;
        this.reconciledAt = reconciledAt;
    }

    public UUID getReconciliationId() {
        return reconciliationId;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public BigDecimal getInternalAmount() {
        return internalAmount;
    }

    public BigDecimal getGatewayAmount() {
        return gatewayAmount;
    }

    public String getInternalStatus() {
        return internalStatus;
    }

    public String getGatewayStatus() {
        return gatewayStatus;
    }

    public ReconciliationStatus getReconciliationStatus() {
        return reconciliationStatus;
    }

    public LocalDateTime getReconciledAt() {
        return reconciledAt;
    }
}