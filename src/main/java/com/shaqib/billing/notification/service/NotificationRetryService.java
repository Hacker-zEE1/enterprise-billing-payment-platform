package com.shaqib.billing.notification.service;

import com.shaqib.billing.notification.entity.Notification;
import com.shaqib.billing.notification.entity.NotificationStatus;
import com.shaqib.billing.notification.repository.NotificationRepository;
import com.shaqib.billing.notification.sender.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationRetryService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationRetryService.class);

    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    public NotificationRetryService(
            NotificationRepository notificationRepository,
            EmailSender emailSender
    ) {
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    public void retryEligibleNotifications() {

        List<Notification> notifications =
                notificationRepository.findByStatusInAndRetryCountLessThan(
                        List.of(
                                NotificationStatus.FAILED,
                                NotificationStatus.PENDING
                        ),
                        MAX_RETRY_COUNT
                );

        LocalDateTime pendingCutoff =
                LocalDateTime.now().minusMinutes(5);

        for (Notification notification : notifications) {

            if (notification.getStatus() == NotificationStatus.PENDING
                    && notification.getCreatedAt().isAfter(pendingCutoff)) {
                continue;
            }

            retry(notification);
        }
    }

    private void retry(Notification notification) {

        notification.prepareForRetry();

        try {

            emailSender.send(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getMessage()
            );

            notification.markSent(LocalDateTime.now());

            log.info(
                    "Notification retry succeeded. notificationId={}, retryCount={}",
                    notification.getNotificationId(),
                    notification.getRetryCount()
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
                    "Notification retry failed. notificationId={}, retryCount={}",
                    notification.getNotificationId(),
                    notification.getRetryCount(),
                    ex
            );
        }

        notificationRepository.save(notification);
    }
}