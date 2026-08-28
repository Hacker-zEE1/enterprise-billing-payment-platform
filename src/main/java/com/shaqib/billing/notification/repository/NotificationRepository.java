package com.shaqib.billing.notification.repository;

import com.shaqib.billing.notification.entity.Notification;
import com.shaqib.billing.notification.entity.NotificationStatus;
import com.shaqib.billing.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByAccountAccountId(
            UUID accountId
    );

    List<Notification> findAllByPaymentPaymentId(
            UUID paymentId
    );

    Optional<Notification>
    findByPaymentPaymentIdAndNotificationType(
            UUID paymentId,
            NotificationType notificationType
    );

    List<Notification> findByStatusInAndRetryCountLessThan(
            List<NotificationStatus> statuses,
            int retryCount
    );
}