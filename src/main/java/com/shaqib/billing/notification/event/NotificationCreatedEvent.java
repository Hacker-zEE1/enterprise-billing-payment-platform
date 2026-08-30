package com.shaqib.billing.notification.event;

import java.util.UUID;

public record NotificationCreatedEvent(UUID notificationId) {
}