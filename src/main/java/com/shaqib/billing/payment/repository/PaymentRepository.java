package com.shaqib.billing.payment.repository;

import com.shaqib.billing.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentIdAndAccountAccountId(
            UUID paymentId,
            UUID accountId
    );

    List<Payment> findAllByAccountAccountId(UUID accountId);
}