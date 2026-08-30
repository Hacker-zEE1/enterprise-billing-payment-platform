package com.shaqib.billing.notification.event;

import com.shaqib.billing.notification.messaging.NotificationEventProducer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationCreatedEventListener {

    private final NotificationEventProducer notificationEventProducer;

    public NotificationCreatedEventListener(
            NotificationEventProducer notificationEventProducer
    ) {
        this.notificationEventProducer = notificationEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {

        notificationEventProducer.publish(
                new NotificationEvent(event.notificationId())
        );
    }
}