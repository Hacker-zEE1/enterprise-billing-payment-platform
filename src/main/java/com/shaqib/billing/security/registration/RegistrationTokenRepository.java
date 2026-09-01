package com.shaqib.billing.security.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationTokenRepository
        extends JpaRepository<RegistrationToken, UUID> {

    Optional<RegistrationToken> findByTokenHash(String tokenHash);

    boolean existsByCustomerCustomerIdAndUsedFalseAndExpiresAtAfter(
            UUID customerId,
            LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT token
        FROM RegistrationToken token
        WHERE token.tokenHash = :tokenHash
        """)
    Optional<RegistrationToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );
}