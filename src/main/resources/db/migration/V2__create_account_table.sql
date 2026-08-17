CREATE TABLE accounts (
                          account_id UUID PRIMARY KEY,
                          customer_id UUID NOT NULL,
                          account_number VARCHAR(30) NOT NULL UNIQUE,
                          account_type VARCHAR(20) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_accounts_customer
                              FOREIGN KEY (customer_id)
                                  REFERENCES customers(customer_id)
);