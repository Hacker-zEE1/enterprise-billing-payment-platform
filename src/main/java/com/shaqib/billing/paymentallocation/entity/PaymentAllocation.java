package com.shaqib.billing.paymentallocation.entity;

import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.payment.entity.Payment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_allocations")
public class PaymentAllocation {

    @Id
    @Column(name = "allocation_id", nullable = false)
    private UUID allocationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(name = "allocated_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PaymentAllocation() {
    }

    public PaymentAllocation(
            UUID allocationId,
            Payment payment,
            Bill bill,
            BigDecimal allocatedAmount,
            LocalDateTime createdAt
    ) {
        this.allocationId = allocationId;
        this.payment = payment;
        this.bill = bill;
        this.allocatedAmount = allocatedAmount;
        this.createdAt = createdAt;
    }

    public UUID getAllocationId() {
        return allocationId;
    }

    public Payment getPayment() {
        return payment;
    }

    public Bill getBill() {
        return bill;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}