package com.shaqib.billing.security.registration;

import com.shaqib.billing.customer.entity.Customer;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registration_tokens")
public class RegistrationToken {

    @Id
    @Column(name = "token_id", nullable = false)
    private UUID tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RegistrationToken() {
    }

    public RegistrationToken(
            UUID tokenId,
            Customer customer,
            String tokenHash,
            LocalDateTime expiresAt,
            boolean used,
            LocalDateTime createdAt
    ) {
        this.tokenId = tokenId;
        this.customer = customer;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}