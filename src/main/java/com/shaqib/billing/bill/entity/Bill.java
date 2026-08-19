package com.shaqib.billing.bill.entity;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.bill.exception.InvalidBillStatusTransitionException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @Column(name = "bill_id", nullable = false)
    private UUID billId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "bill_number", nullable = false, unique = true, length = 30)
    private String billNumber;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BillStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Bill() {
    }

    public Bill(
            UUID billId,
            Account account,
            String billNumber,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd,
            LocalDate dueDate,
            BigDecimal totalAmount,
            BillStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.billId = billId;
        this.account = account;
        this.billNumber = billNumber;
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getBillId() {
        return billId;
    }

    public Account getAccount() {
        return account;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public LocalDate getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public LocalDate getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BillStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }





    public void issue(LocalDateTime updatedAt) {

        if (this.status != BillStatus.DRAFT) {
            throw new InvalidBillStatusTransitionException(
                    "Only DRAFT bills can be issued"
            );
        }

        this.status = BillStatus.ISSUED;
        this.updatedAt = updatedAt;
    }

    public void cancel(LocalDateTime updatedAt) {

        if (this.status == BillStatus.PAID) {
            throw new InvalidBillStatusTransitionException(
                    "Paid bills cannot be cancelled"
            );
        }

        if (this.status == BillStatus.CANCELLED) {
            return;
        }

        this.status = BillStatus.CANCELLED;
        this.updatedAt = updatedAt;
    }
}