package com.walletplatform.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionResponse {
    private UUID id;
    private UUID sourceWalletId;
    private UUID targetWalletId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String sourceCurrency;
    private BigDecimal convertedAmount;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public TransactionResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceWalletId() { return sourceWalletId; }
    public void setSourceWalletId(UUID sourceWalletId) { this.sourceWalletId = sourceWalletId; }
    public UUID getTargetWalletId() { return targetWalletId; }
    public void setTargetWalletId(UUID targetWalletId) { this.targetWalletId = targetWalletId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getSourceCurrency() { return sourceCurrency; }
    public void setSourceCurrency(String sourceCurrency) { this.sourceCurrency = sourceCurrency; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }
    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TransactionResponse r = new TransactionResponse();
        public Builder id(UUID id) { r.id = id; return this; }
        public Builder sourceWalletId(UUID sourceWalletId) { r.sourceWalletId = sourceWalletId; return this; }
        public Builder targetWalletId(UUID targetWalletId) { r.targetWalletId = targetWalletId; return this; }
        public Builder type(String type) { r.type = type; return this; }
        public Builder status(String status) { r.status = status; return this; }
        public Builder amount(BigDecimal amount) { r.amount = amount; return this; }
        public Builder sourceCurrency(String sourceCurrency) { r.sourceCurrency = sourceCurrency; return this; }
        public Builder convertedAmount(BigDecimal convertedAmount) { r.convertedAmount = convertedAmount; return this; }
        public Builder targetCurrency(String targetCurrency) { r.targetCurrency = targetCurrency; return this; }
        public Builder exchangeRate(BigDecimal exchangeRate) { r.exchangeRate = exchangeRate; return this; }
        public Builder description(String description) { r.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { r.createdAt = createdAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { r.completedAt = completedAt; return this; }
        public TransactionResponse build() { return r; }
    }
}
