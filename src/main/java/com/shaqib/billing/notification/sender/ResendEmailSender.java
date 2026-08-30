package com.shaqib.billing.notification.sender;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cloud")
public class ResendEmailSender implements EmailSender {

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailSender(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail
    ) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(
            String recipient,
            String subject,
            String message
    ) {

        CreateEmailOptions email =
                CreateEmailOptions.builder()
                        .from(fromEmail)
                        .to(recipient)
                        .subject(subject)
                        .text(message)
                        .build();

        try {
            CreateEmailResponse response =
                    resend.emails().send(email);

        } catch (ResendException ex) {
            throw new RuntimeException(
                    "Failed to send email through Resend",
                    ex
            );
        }
    }
}