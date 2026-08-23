package com.shaqib.billing.payment.entity;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.bill.entity.Bill;
import jakarta.persistence.*;
import com.shaqib.billing.payment.exception.InvalidPaymentStatusTransitionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 50)
    private String paymentReference;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", length = 30)
    private PaymentGatewayProvider gateway;

    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    protected Payment() {
    }

    public Payment(
            UUID paymentId,
            Account account,
            Bill bill,
            String paymentReference,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            LocalDateTime paymentDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.paymentId = paymentId;
        this.account = account;
        this.bill = bill;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = paymentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public Account getAccount() {
        return account;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PaymentGatewayProvider getGateway() {
        return gateway;
    }

    public String getGatewayOrderId() {
        return gatewayOrderId;
    }

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public Bill getBill() {
        return bill;
    }

    public void markSuccess(LocalDateTime updatedAt) {

        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only PENDING payments can be marked as SUCCESS"
            );
        }

        this.status = PaymentStatus.SUCCESS;
        this.updatedAt = updatedAt;
    }

    public void markFailed(LocalDateTime updatedAt) {

        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only PENDING payments can be marked as FAILED"
            );
        }

        this.status = PaymentStatus.FAILED;
        this.updatedAt = updatedAt;
    }

    public void cancel(LocalDateTime updatedAt) {

        if (this.status == PaymentStatus.CANCELLED) {
            return;
        }

        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only PENDING payments can be cancelled"
            );
        }

        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = updatedAt;
    }


    public void assignGatewayOrder(
            PaymentGatewayProvider gateway,
            String gatewayOrderId,
            LocalDateTime updatedAt
    ) {
        this.gateway = gateway;
        this.gatewayOrderId = gatewayOrderId;
        this.updatedAt = updatedAt;
    }

    public void completeGatewayPayment(
            String gatewayPaymentId,
            LocalDateTime updatedAt
    ) {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only PENDING payments can be completed"
            );
        }

        this.gatewayPaymentId = gatewayPaymentId;
        this.status = PaymentStatus.SUCCESS;
        this.updatedAt = updatedAt;
    }
}