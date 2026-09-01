ALTER TABLE app_users
    ADD COLUMN customer_id UUID;

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers(customer_id);

ALTER TABLE app_users
    ADD CONSTRAINT uk_app_users_customer
        UNIQUE (customer_id);