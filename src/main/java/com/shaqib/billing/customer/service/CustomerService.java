package com.shaqib.billing.customer.service;

import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.customer.entity.CustomerStatus;
import com.shaqib.billing.customer.exception.DuplicateCustomerEmailException;
import com.shaqib.billing.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    ) {
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateCustomerEmailException(
                    "Customer with this email already exists"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer(
                UUID.randomUUID(),
                firstName,
                lastName,
                email,
                phoneNumber,
                CustomerStatus.ACTIVE,
                now,
                now
        );

        return customerRepository.save(customer);
    }
}