package com.walletplatform.shared.event;

import java.math.BigDecimal;
import java.util.UUID;

public class LedgerEntriesCreatedEvent extends DomainEvent {
    
    private final UUID transactionId;
    private final int entryCount;
    private final BigDecimal totalAmount;
    private final String operationType;
    
    public LedgerEntriesCreatedEvent(UUID correlationId, UUID transactionId,
                                      int entryCount, BigDecimal totalAmount,
                                      String operationType) {
        super(correlationId);
        this.transactionId = transactionId;
        this.entryCount = entryCount;
        this.totalAmount = totalAmount;
        this.operationType = operationType;
    }
    
    @Override
    public String getEventType() { return "LEDGER_ENTRIES_CREATED"; }
    
    public UUID getTransactionId() { return transactionId; }
    public int getEntryCount() { return entryCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getOperationType() { return operationType; }
}
