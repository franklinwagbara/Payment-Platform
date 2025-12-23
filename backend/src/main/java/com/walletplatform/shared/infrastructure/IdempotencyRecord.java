package com.walletplatform.shared.infrastructure;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores idempotency keys and their results for replay protection.
 */
@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true)
})
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "result_type", nullable = false)
    private String resultType;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "status", nullable = false)
    private String status = "COMPLETED";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String resultType, String resultJson) {
        this.idempotencyKey = idempotencyKey;
        this.resultType = resultType;
        this.resultJson = resultJson;
        this.expiresAt = LocalDateTime.now().plusDays(7); 
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getResultType() { return resultType; }
    public String getResultJson() { return resultJson; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setStatus(String status) { this.status = status; }
}
