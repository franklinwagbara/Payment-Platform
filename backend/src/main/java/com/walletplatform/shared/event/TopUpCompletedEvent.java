package com.walletplatform.shared.event;

import java.math.BigDecimal;
import java.util.UUID;

public class TopUpCompletedEvent extends DomainEvent {
    
    private final UUID transactionId;
    private final UUID walletId;
    private final UUID userId;
    private final BigDecimal amount;
    private final String currency;
    
    public TopUpCompletedEvent(UUID correlationId, UUID transactionId, UUID walletId, 
                                UUID userId, BigDecimal amount, String currency) {
        super(correlationId);
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
    }
    
    @Override
    public String getEventType() { return "TOP_UP_COMPLETED"; }
    
    public UUID getTransactionId() { return transactionId; }
    public UUID getWalletId() { return walletId; }
    public UUID getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
