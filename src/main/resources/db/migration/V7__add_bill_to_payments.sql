ALTER TABLE payments
    ADD COLUMN bill_id UUID;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_bill
        FOREIGN KEY (bill_id)
            REFERENCES bills(bill_id);