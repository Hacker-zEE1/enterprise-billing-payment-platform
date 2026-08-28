package com.shaqib.billing.notification.scheduler;

import com.shaqib.billing.notification.service.NotificationRetryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetryScheduler {

    private final NotificationRetryService notificationRetryService;

    public NotificationRetryScheduler(
            NotificationRetryService notificationRetryService
    ) {
        this.notificationRetryService = notificationRetryService;
    }

    @Scheduled(
            fixedDelayString = "${notification.retry.delay-ms:60000}"
    )
    public void retryNotifications() {
        notificationRetryService.retryEligibleNotifications();
    }
}