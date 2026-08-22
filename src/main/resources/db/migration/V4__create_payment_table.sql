CREATE TABLE payments (
                          payment_id UUID PRIMARY KEY,
                          account_id UUID NOT NULL,
                          payment_reference VARCHAR(50) NOT NULL UNIQUE,
                          amount NUMERIC(18,2) NOT NULL,
                          payment_method VARCHAR(30) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          payment_date TIMESTAMP NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_payments_account
                              FOREIGN KEY (account_id)
                                  REFERENCES accounts(account_id)
);