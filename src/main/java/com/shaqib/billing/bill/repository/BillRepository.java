package com.shaqib.billing.bill.repository;

import com.shaqib.billing.bill.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    Optional<Bill> findByBillIdAndAccountAccountId(
            UUID billId,
            UUID accountId
    );

    List<Bill> findAllByAccountAccountId(UUID accountId);
}