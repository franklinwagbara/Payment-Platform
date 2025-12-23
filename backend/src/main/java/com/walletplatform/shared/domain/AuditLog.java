package com.walletplatform.shared.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "text")
    private Map<String, Object> details;

    @Column(nullable = false)
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public Map<String, Object> getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setAction(String action) { this.action = action; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID userId;
        private String action;
        private String entityType;
        private UUID entityId;
        private Map<String, Object> details;
        private String ipAddress;

        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(UUID entityId) { this.entityId = entityId; return this; }
        public Builder details(Map<String, Object> details) { this.details = details; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.userId = this.userId;
            log.action = this.action;
            log.entityType = this.entityType;
            log.entityId = this.entityId;
            log.details = this.details;
            log.ipAddress = this.ipAddress;
            return log;
        }
    }
}
