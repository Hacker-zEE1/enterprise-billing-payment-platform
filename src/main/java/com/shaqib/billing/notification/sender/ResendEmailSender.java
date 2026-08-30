package com.shaqib.billing.notification.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Profile("cloud")
public class ResendEmailSender implements EmailSender {

    private final RestClient restClient;
    private final String fromEmail;

    public ResendEmailSender(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail
    ) {
        this.fromEmail = fromEmail;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(
            String recipient,
            String subject,
            String message
    ) {

        Map<String, Object> request = Map.of(
                "from", fromEmail,
                "to", List.of(recipient),
                "subject", subject,
                "text", message
        );

        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}