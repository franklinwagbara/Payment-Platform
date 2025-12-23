package com.walletplatform.shared.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {
    
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
