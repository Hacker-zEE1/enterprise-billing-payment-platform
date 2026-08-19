CREATE TABLE bills (
                       bill_id UUID PRIMARY KEY,
                       account_id UUID NOT NULL,
                       bill_number VARCHAR(30) NOT NULL UNIQUE,
                       billing_period_start DATE NOT NULL,
                       billing_period_end DATE NOT NULL,
                       due_date DATE NOT NULL,
                       total_amount NUMERIC(18,2) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,

                       CONSTRAINT fk_bills_account
                           FOREIGN KEY (account_id)
                               REFERENCES accounts(account_id)
);