package com.walletplatform.shared.event;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawalCompletedEvent extends DomainEvent {
    
    private final UUID transactionId;
    private final UUID walletId;
    private final UUID userId;
    private final BigDecimal amount;
    private final String currency;
    private final String bankName;
    
    public WithdrawalCompletedEvent(UUID correlationId, UUID transactionId, UUID walletId,
                                     UUID userId, BigDecimal amount, String currency, String bankName) {
        super(correlationId);
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.bankName = bankName;
    }
    
    @Override
    public String getEventType() { return "WITHDRAWAL_COMPLETED"; }
    
    public UUID getTransactionId() { return transactionId; }
    public UUID getWalletId() { return walletId; }
    public UUID getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getBankName() { return bankName; }
}
