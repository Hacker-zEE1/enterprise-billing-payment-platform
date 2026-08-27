package com.shaqib.billing.financialtransaction.entity;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.payment.entity.Payment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {

    @Id
    @Column(name = "financial_transaction_id", nullable = false)
    private UUID financialTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private FinancialTransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference", nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected FinancialTransaction() {
    }

    public FinancialTransaction(
            UUID financialTransactionId,
            Account account,
            Payment payment,
            Bill bill,
            FinancialTransactionType transactionType,
            BigDecimal amount,
            String reference,
            LocalDateTime createdAt
    ) {
        this.financialTransactionId = financialTransactionId;
        this.account = account;
        this.payment = payment;
        this.bill = bill;
        this.transactionType = transactionType;
        this.amount = amount;
        this.reference = reference;
        this.createdAt = createdAt;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    public Account getAccount() {
        return account;
    }

    public Payment getPayment() {
        return payment;
    }

    public Bill getBill() {
        return bill;
    }

    public FinancialTransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}