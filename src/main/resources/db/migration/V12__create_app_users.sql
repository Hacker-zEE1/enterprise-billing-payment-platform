CREATE TABLE app_users (
   user_id UUID PRIMARY KEY,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(30) NOT NULL,
   enabled BOOLEAN NOT NULL,
   created_at TIMESTAMP NOT NULL
);