package com.shaqib.billing.notification.sender;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;

    public SmtpEmailSender(
            JavaMailSender javaMailSender
    ) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(
            String recipient,
            String subject,
            String message
    ) {

        SimpleMailMessage mailMessage =
                new SimpleMailMessage();

        mailMessage.setTo(recipient);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);
    }
}