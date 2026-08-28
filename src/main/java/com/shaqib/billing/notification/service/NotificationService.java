package com.shaqib.billing.notification.service;

import com.shaqib.billing.notification.entity.Notification;
import com.shaqib.billing.notification.entity.NotificationChannel;
import com.shaqib.billing.notification.entity.NotificationStatus;
import com.shaqib.billing.notification.entity.NotificationType;
import com.shaqib.billing.notification.event.NotificationEvent;
import com.shaqib.billing.notification.messaging.NotificationEventProducer;
import com.shaqib.billing.notification.repository.NotificationRepository;
import com.shaqib.billing.payment.entity.Payment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventProducer notificationEventProducer;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationEventProducer notificationEventProducer
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationEventProducer = notificationEventProducer;
    }

    public Notification createPaymentSuccessNotification(
            Payment payment
    ) {

        return notificationRepository
                .findByPaymentPaymentIdAndNotificationType(
                        payment.getPaymentId(),
                        NotificationType.PAYMENT_SUCCESS
                )
                .orElseGet(() -> createNotification(payment));
    }

    private Notification createNotification(
            Payment payment
    ) {

        String recipient =
                payment.getAccount()
                        .getCustomer()
                        .getEmail();

        String subject =
                "Payment received successfully";

        String message =
                "Your payment of ₹"
                        + payment.getAmount()
                        + " has been received successfully.";

        Notification notification =
                new Notification(
                UUID.randomUUID(),
                payment.getAccount(),
                payment,
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.EMAIL,
                NotificationStatus.PENDING,
                recipient,
                "Payment received successfully",
                message,
                LocalDateTime.now(),
                null,
                0,
                null,
                null
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        notificationEventProducer.publish(
                new NotificationEvent(
                        savedNotification.getNotificationId()
                )
        );

        return savedNotification;
    }
}