package com.shaqib.billing.notification.sender;

public interface EmailSender {

    void send(
            String recipient,
            String subject,
            String message
    );
}