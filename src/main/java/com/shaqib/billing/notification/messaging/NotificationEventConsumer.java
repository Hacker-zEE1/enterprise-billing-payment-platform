package com.shaqib.billing.notification.messaging;

import com.shaqib.billing.notification.entity.Notification;
import com.shaqib.billing.notification.entity.NotificationStatus;
import com.shaqib.billing.notification.event.NotificationEvent;
import com.shaqib.billing.notification.repository.NotificationRepository;
import com.shaqib.billing.notification.sender.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    public NotificationEventConsumer(
            NotificationRepository notificationRepository,
            EmailSender emailSender
    ) {
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    @KafkaListener(
            topics = "payment-notification",
            groupId = "notification-service"
    )
    public void consume(NotificationEvent event) {

        Notification notification =
                notificationRepository
                        .findById(event.notificationId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Notification not found: "
                                                + event.notificationId()
                                )
                        );

        if (notification.getStatus()
                != NotificationStatus.PENDING) {
            return;
        }

        try {
            emailSender.send(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getMessage()
            );

            notification.markSent(LocalDateTime.now());

            log.info(
                    "Notification sent successfully. notificationId={}",
                    notification.getNotificationId()
            );

        } catch (Exception ex) {

            String failureReason = ex.getMessage();

            if (failureReason != null && failureReason.length() > 500) {
                failureReason = failureReason.substring(0, 500);
            }

            notification.markFailed(
                    LocalDateTime.now(),
                    failureReason
            );
            log.error(
                    "Notification sending failed. notificationId={}",
                    notification.getNotificationId(),
                    ex
            );
        }

        notificationRepository.save(notification);
    }
}