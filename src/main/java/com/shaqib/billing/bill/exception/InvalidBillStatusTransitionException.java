package com.shaqib.billing.bill.exception;

public class InvalidBillStatusTransitionException extends RuntimeException {

    public InvalidBillStatusTransitionException(String message) {
        super(message);
    }
}