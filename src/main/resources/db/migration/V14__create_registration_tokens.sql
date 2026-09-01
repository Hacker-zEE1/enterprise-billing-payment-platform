CREATE TABLE registration_tokens (
     token_id UUID PRIMARY KEY,
     customer_id UUID NOT NULL,
     token_hash VARCHAR(255) NOT NULL UNIQUE,
     expires_at TIMESTAMP NOT NULL,
     used BOOLEAN NOT NULL DEFAULT FALSE,
     created_at TIMESTAMP NOT NULL,

     CONSTRAINT fk_registration_tokens_customer
         FOREIGN KEY (customer_id)
             REFERENCES customers(customer_id)
);

CREATE INDEX idx_registration_tokens_customer_id
    ON registration_tokens(customer_id);