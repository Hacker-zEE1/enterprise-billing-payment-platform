package com.shaqib.billing.notification.messaging;

import com.shaqib.billing.notification.event.NotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventProducer {

    private static final String TOPIC =
            "payment-notification";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationEventProducer(
            KafkaTemplate<String, NotificationEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            NotificationEvent event
    ) {

        kafkaTemplate.send(
                TOPIC,
                event.notificationId().toString(),
                event
        );
    }
}