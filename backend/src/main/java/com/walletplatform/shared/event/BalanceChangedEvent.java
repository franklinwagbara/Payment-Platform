package com.walletplatform.shared.event;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceChangedEvent extends DomainEvent {
    
    private final UUID walletId;
    private final UUID ownerId;
    private final BigDecimal previousBalance;
    private final BigDecimal newBalance;
    private final String currency;
    private final String reason; 
    
    public BalanceChangedEvent(UUID correlationId, UUID walletId, UUID ownerId,
                               BigDecimal previousBalance, BigDecimal newBalance,
                               String currency, String reason) {
        super(correlationId);
        this.walletId = walletId;
        this.ownerId = ownerId;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.currency = currency;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() { return "BALANCE_CHANGED"; }
    
    public UUID getWalletId() { return walletId; }
    public UUID getOwnerId() { return ownerId; }
    public BigDecimal getPreviousBalance() { return previousBalance; }
    public BigDecimal getNewBalance() { return newBalance; }
    public String getCurrency() { return currency; }
    public String getReason() { return reason; }
}
