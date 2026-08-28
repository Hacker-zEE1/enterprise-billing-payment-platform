package com.shaqib.billing.notification.event;

import java.util.UUID;

public record NotificationEvent(
        UUID notificationId
) {
}