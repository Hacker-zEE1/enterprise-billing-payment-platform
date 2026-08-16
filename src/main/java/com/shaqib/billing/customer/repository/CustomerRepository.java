package com.shaqib.billing.customer.repository;

import com.shaqib.billing.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndCustomerIdNot(String email, UUID customerId);
}