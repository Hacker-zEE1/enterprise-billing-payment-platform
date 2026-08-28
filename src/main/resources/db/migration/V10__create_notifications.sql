CREATE TABLE notifications (
   notification_id UUID PRIMARY KEY,
   account_id UUID NOT NULL,
   payment_id UUID,
   notification_type VARCHAR(30) NOT NULL,
   channel VARCHAR(20) NOT NULL,
   status VARCHAR(20) NOT NULL,
   recipient VARCHAR(255) NOT NULL,
   subject VARCHAR(255) NOT NULL,
   message TEXT NOT NULL,
   created_at TIMESTAMP NOT NULL,
   sent_at TIMESTAMP,

   CONSTRAINT fk_notifications_account
       FOREIGN KEY (account_id)
           REFERENCES accounts(account_id),

   CONSTRAINT fk_notifications_payment
       FOREIGN KEY (payment_id)
           REFERENCES payments(payment_id)
);