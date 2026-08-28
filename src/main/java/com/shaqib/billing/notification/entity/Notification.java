package com.shaqib.billing.notification.entity;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.payment.entity.Payment;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    protected Notification() {
    }

    public Notification(
            UUID notificationId,
            Account account,
            Payment payment,
            NotificationType notificationType,
            NotificationChannel channel,
            NotificationStatus status,
            String recipient,
            String subject,
            String message,
            LocalDateTime createdAt,
            LocalDateTime sentAt,
            int retryCount,
            LocalDateTime lastAttemptAt,
            String failureReason
    ) {
        this.notificationId = notificationId;
        this.account = account;
        this.payment = payment;
        this.notificationType = notificationType;
        this.channel = channel;
        this.status = status;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.retryCount = retryCount;
        this.lastAttemptAt = lastAttemptAt;
        this.failureReason = failureReason;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public Account getAccount() {
        return account;
    }

    public Payment getPayment() {
        return payment;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public String getFailureReason() {
        return failureReason;
    }


    public void markSent(LocalDateTime sentAt) {
        this.status = NotificationStatus.SENT;
        this.sentAt = sentAt;
        this.lastAttemptAt = sentAt;
        this.failureReason = null;
    }

    public void markFailed(LocalDateTime attemptedAt, String failureReason) {
        this.status = NotificationStatus.FAILED;
        this.lastAttemptAt = attemptedAt;
        this.failureReason = failureReason;
    }

    public void prepareForRetry() {
        this.status = NotificationStatus.PENDING;
        this.retryCount++;
    }
}