CREATE TABLE financial_transactions (
    financial_transaction_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    payment_id UUID,
    bill_id UUID,
    transaction_type VARCHAR(30) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    reference VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_financial_transactions_account
        FOREIGN KEY (account_id)
            REFERENCES accounts(account_id),

    CONSTRAINT fk_financial_transactions_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments(payment_id),

    CONSTRAINT fk_financial_transactions_bill
        FOREIGN KEY (bill_id)
            REFERENCES bills(bill_id)
);