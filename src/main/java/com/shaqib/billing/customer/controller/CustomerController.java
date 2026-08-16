package com.shaqib.billing.customer.controller;

import com.shaqib.billing.customer.dto.CreateCustomerRequest;
import com.shaqib.billing.customer.dto.CustomerResponse;
import com.shaqib.billing.customer.dto.UpdateCustomerRequest;
import com.shaqib.billing.customer.entity.Customer;
import com.shaqib.billing.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        Customer customer = customerService.createCustomer(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber()
        );

        CustomerResponse response = toResponse(customer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable UUID customerId
    ) {

        Customer customer = customerService.getCustomerById(customerId);

        CustomerResponse response = toResponse(customer);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

        List<CustomerResponse> responses = customerService.getAllCustomers()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {

        Customer customer = customerService.updateCustomer(
                customerId,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber()
        );

        CustomerResponse response = toResponse(customer);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{customerId}/deactivate")
    public ResponseEntity<CustomerResponse> deactivateCustomer(
            @PathVariable UUID customerId
    ) {

        Customer customer = customerService.deactivateCustomer(customerId);

        CustomerResponse response = toResponse(customer);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{customerId}/activate")
    public ResponseEntity<CustomerResponse> activateCustomer(
            @PathVariable UUID customerId
    ) {

        Customer customer = customerService.activateCustomer(customerId);

        CustomerResponse response = toResponse(customer);

        return ResponseEntity.ok(response);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

}