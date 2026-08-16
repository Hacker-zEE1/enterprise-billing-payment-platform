package com.shaqib.billing.customer.exception;

public class DuplicateCustomerEmailException extends RuntimeException {

    public DuplicateCustomerEmailException(String message) {
        super(message);
    }
}