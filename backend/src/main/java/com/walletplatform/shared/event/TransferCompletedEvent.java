package com.walletplatform.shared.event;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferCompletedEvent extends DomainEvent {
    
    private final UUID transactionId;
    private final UUID sourceWalletId;
    private final UUID targetWalletId;
    private final BigDecimal sourceAmount;
    private final BigDecimal targetAmount;
    private final String sourceCurrency;
    private final String targetCurrency;
    
    public TransferCompletedEvent(UUID correlationId, UUID transactionId, 
                                   UUID sourceWalletId, UUID targetWalletId,
                                   BigDecimal sourceAmount, BigDecimal targetAmount,
                                   String sourceCurrency, String targetCurrency) {
        super(correlationId);
        this.transactionId = transactionId;
        this.sourceWalletId = sourceWalletId;
        this.targetWalletId = targetWalletId;
        this.sourceAmount = sourceAmount;
        this.targetAmount = targetAmount;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
    }
    
    @Override
    public String getEventType() { return "TRANSFER_COMPLETED"; }
    
    public UUID getTransactionId() { return transactionId; }
    public UUID getSourceWalletId() { return sourceWalletId; }
    public UUID getTargetWalletId() { return targetWalletId; }
    public BigDecimal getSourceAmount() { return sourceAmount; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public String getSourceCurrency() { return sourceCurrency; }
    public String getTargetCurrency() { return targetCurrency; }
}
