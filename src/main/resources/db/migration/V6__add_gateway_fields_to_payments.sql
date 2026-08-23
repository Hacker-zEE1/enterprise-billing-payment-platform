ALTER TABLE payments
    ADD COLUMN gateway VARCHAR(30),
    ADD COLUMN gateway_order_id VARCHAR(100),
    ADD COLUMN gateway_payment_id VARCHAR(100);