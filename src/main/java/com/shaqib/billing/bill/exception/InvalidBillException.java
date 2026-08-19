package com.shaqib.billing.bill.exception;

public class InvalidBillException extends RuntimeException {

    public InvalidBillException(String message) {
        super(message);
    }
}