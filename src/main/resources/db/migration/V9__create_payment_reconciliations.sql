CREATE TABLE payment_reconciliations (
     reconciliation_id UUID PRIMARY KEY,
     payment_id UUID NOT NULL,
     gateway_payment_id VARCHAR(100),
     internal_amount NUMERIC(18,2) NOT NULL,
     gateway_amount NUMERIC(18,2),
     internal_status VARCHAR(30) NOT NULL,
     gateway_status VARCHAR(30),
     reconciliation_status VARCHAR(30) NOT NULL,
     reconciled_at TIMESTAMP NOT NULL,

     CONSTRAINT fk_payment_reconciliations_payment
         FOREIGN KEY (payment_id)
             REFERENCES payments(payment_id)
);