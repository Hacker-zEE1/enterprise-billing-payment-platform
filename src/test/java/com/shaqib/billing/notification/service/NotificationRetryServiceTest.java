package com.shaqib.billing.notification.service;

import com.shaqib.billing.notification.entity.Notification;
import com.shaqib.billing.notification.entity.NotificationStatus;
import com.shaqib.billing.notification.repository.NotificationRepository;
import com.shaqib.billing.notification.sender.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationRetryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailSender emailSender;

    private NotificationRetryService notificationRetryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        notificationRetryService =
                new NotificationRetryService(
                        notificationRepository,
                        emailSender
                );
    }

    @Test
    void shouldRetryFailedNotificationSuccessfully() {

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.FAILED);

        when(notification.getRecipient())
                .thenReturn("test@example.com");

        when(notification.getSubject())
                .thenReturn("Payment received successfully");

        when(notification.getMessage())
                .thenReturn("Your payment has been received successfully.");

        when(notification.getRetryCount())
                .thenReturn(1);

        when(notificationRepository.findByStatusInAndRetryCountLessThan(
                anyList(),
                eq(3)
        )).thenReturn(List.of(notification));

        notificationRetryService.retryEligibleNotifications();

        verify(notification, times(1))
                .prepareForRetry();

        verify(emailSender, times(1))
                .send(
                        "test@example.com",
                        "Payment received successfully",
                        "Your payment has been received successfully."
                );

        verify(notification, times(1))
                .markSent(any(LocalDateTime.class));

        verify(notificationRepository, times(1))
                .save(notification);
    }

    @Test
    void shouldSkipRecentPendingNotification() {

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.PENDING);

        when(notification.getCreatedAt())
                .thenReturn(LocalDateTime.now());

        when(notificationRepository.findByStatusInAndRetryCountLessThan(
                anyList(),
                eq(3)
        )).thenReturn(List.of(notification));

        notificationRetryService.retryEligibleNotifications();

        verify(notification, never())
                .prepareForRetry();

        verify(emailSender, never())
                .send(anyString(), anyString(), anyString());

        verify(notification, never())
                .markSent(any(LocalDateTime.class));

        verify(notificationRepository, never())
                .save(any());
    }

    @Test
    void shouldRetryStalePendingNotification() {

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.PENDING);

        when(notification.getCreatedAt())
                .thenReturn(LocalDateTime.now().minusMinutes(10));

        when(notification.getRecipient())
                .thenReturn("test@example.com");

        when(notification.getSubject())
                .thenReturn("Payment received successfully");

        when(notification.getMessage())
                .thenReturn("Your payment has been received successfully.");

        when(notification.getRetryCount())
                .thenReturn(1);

        when(notificationRepository.findByStatusInAndRetryCountLessThan(
                anyList(),
                eq(3)
        )).thenReturn(List.of(notification));

        notificationRetryService.retryEligibleNotifications();

        verify(notification, times(1))
                .prepareForRetry();

        verify(emailSender, times(1))
                .send(
                        "test@example.com",
                        "Payment received successfully",
                        "Your payment has been received successfully."
                );

        verify(notification, times(1))
                .markSent(any(LocalDateTime.class));

        verify(notificationRepository, times(1))
                .save(notification);
    }

    @Test
    void shouldMarkNotificationFailedWhenRetryFails() {

        Notification notification = mock(Notification.class);

        when(notification.getStatus())
                .thenReturn(NotificationStatus.FAILED);

        when(notification.getRecipient())
                .thenReturn("test@example.com");

        when(notification.getSubject())
                .thenReturn("Payment received successfully");

        when(notification.getMessage())
                .thenReturn("Your payment has been received successfully.");

        when(notification.getRetryCount())
                .thenReturn(2);

        when(notificationRepository.findByStatusInAndRetryCountLessThan(
                anyList(),
                eq(3)
        )).thenReturn(List.of(notification));

        doThrow(new RuntimeException("SMTP retry failure"))
                .when(emailSender)
                .send(
                        "test@example.com",
                        "Payment received successfully",
                        "Your payment has been received successfully."
                );

        notificationRetryService.retryEligibleNotifications();

        verify(notification, times(1))
                .prepareForRetry();

        verify(notification, never())
                .markSent(any(LocalDateTime.class));

        verify(notification, times(1))
                .markFailed(
                        any(LocalDateTime.class),
                        eq("SMTP retry failure")
                );

        verify(notificationRepository, times(1))
                .save(notification);
    }
}