package com.shaqib.billing.paymentallocation.repository;

import com.shaqib.billing.paymentallocation.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentAllocationRepository
        extends JpaRepository<PaymentAllocation, UUID> {

    List<PaymentAllocation> findAllByPaymentPaymentId(UUID paymentId);

    List<PaymentAllocation> findAllByBillBillId(UUID billId);

    boolean existsByPaymentPaymentIdAndBillBillId(
            UUID paymentId,
            UUID billId
    );
}