package com.shaqib.billing.notification.messaging;

import com.shaqib.billing.notification.entity.Notification;
import com.shaqib.billing.notification.entity.NotificationStatus;
import com.shaqib.billing.notification.event.NotificationEvent;
import com.shaqib.billing.notification.repository.NotificationRepository;
import com.shaqib.billing.notification.sender.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailSender emailSender;

    private NotificationEventConsumer notificationEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        notificationEventConsumer =
                new NotificationEventConsumer(
                        notificationRepository,
                        emailSender
                );
    }

    @Test
    void shouldSendPendingNotificationSuccessfully() {

        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.PENDING);

        when(notification.getNotificationId())
                .thenReturn(notificationId);

        when(notification.getRecipient())
                .thenReturn("test@example.com");

        when(notification.getSubject())
                .thenReturn("Payment received successfully");

        when(notification.getMessage())
                .thenReturn("Your payment has been received successfully.");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        NotificationEvent event =
                new NotificationEvent(notificationId);

        notificationEventConsumer.consume(event);

        verify(emailSender, times(1))
                .send(
                        "test@example.com",
                        "Payment received successfully",
                        "Your payment has been received successfully."
                );

        verify(notification, times(1))
                .markSent(any());

        verify(notificationRepository, times(1))
                .save(notification);
    }


    @Test
    void shouldMarkNotificationFailedWhenEmailSendingFails() {

        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.PENDING);

        when(notification.getNotificationId())
                .thenReturn(notificationId);

        when(notification.getRecipient())
                .thenReturn("test@example.com");

        when(notification.getSubject())
                .thenReturn("Payment received successfully");

        when(notification.getMessage())
                .thenReturn("Your payment has been received successfully.");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        doThrow(new RuntimeException("SMTP failure"))
                .when(emailSender)
                .send(
                        "test@example.com",
                        "Payment received successfully",
                        "Your payment has been received successfully."
                );

        NotificationEvent event =
                new NotificationEvent(notificationId);

        notificationEventConsumer.consume(event);

        verify(notification, times(1))
                .markFailed(any(), eq("SMTP failure"));

        verify(notification, never())
                .markSent(any());

        verify(notificationRepository, times(1))
                .save(notification);
    }


    @Test
    void shouldSkipAlreadySentNotification() {

        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.SENT);

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        NotificationEvent event =
                new NotificationEvent(notificationId);

        notificationEventConsumer.consume(event);

        verify(emailSender, never())
                .send(anyString(), anyString(), anyString());

        verify(notification, never())
                .markSent(any());

        verify(notification, never())
                .markFailed(any(), anyString());

        verify(notificationRepository, never())
                .save(any());
    }
}