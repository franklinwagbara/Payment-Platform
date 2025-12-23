package com.walletplatform.shared.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.UUID;

public class AuditEvent extends ApplicationEvent {
    
    private final UUID userId;
    private final String action;
    private final String entityType;
    private final UUID entityId;
    private final Map<String, Object> details;
    private final String ipAddress;

    public AuditEvent(Object source, UUID userId, String action, String entityType, 
                      UUID entityId, Map<String, Object> details, String ipAddress) {
        super(source);
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public UUID getUserId() { return userId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public Map<String, Object> getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
}
