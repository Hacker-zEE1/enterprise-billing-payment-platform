CREATE TABLE payment_allocations (
     allocation_id UUID PRIMARY KEY,
     payment_id UUID NOT NULL,
     bill_id UUID NOT NULL,
     allocated_amount NUMERIC(18,2) NOT NULL,
     created_at TIMESTAMP NOT NULL,

     CONSTRAINT fk_payment_allocations_payment
         FOREIGN KEY (payment_id)
             REFERENCES payments(payment_id),

     CONSTRAINT fk_payment_allocations_bill
         FOREIGN KEY (bill_id)
             REFERENCES bills(bill_id)
);