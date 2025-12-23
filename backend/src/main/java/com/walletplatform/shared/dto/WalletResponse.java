package com.walletplatform.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WalletResponse {
    private UUID id;
    private String currency;
    private String currencySymbol;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private BigDecimal spentToday;
    private BigDecimal remainingDailyLimit;
    private boolean active;
    private LocalDateTime createdAt;

    public WalletResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
    public BigDecimal getSpentToday() { return spentToday; }
    public void setSpentToday(BigDecimal spentToday) { this.spentToday = spentToday; }
    public BigDecimal getRemainingDailyLimit() { return remainingDailyLimit; }
    public void setRemainingDailyLimit(BigDecimal remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final WalletResponse response = new WalletResponse();
        public Builder id(UUID id) { response.id = id; return this; }
        public Builder currency(String currency) { response.currency = currency; return this; }
        public Builder currencySymbol(String currencySymbol) { response.currencySymbol = currencySymbol; return this; }
        public Builder balance(BigDecimal balance) { response.balance = balance; return this; }
        public Builder dailyLimit(BigDecimal dailyLimit) { response.dailyLimit = dailyLimit; return this; }
        public Builder spentToday(BigDecimal spentToday) { response.spentToday = spentToday; return this; }
        public Builder remainingDailyLimit(BigDecimal remainingDailyLimit) { response.remainingDailyLimit = remainingDailyLimit; return this; }
        public Builder active(boolean active) { response.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt) { response.createdAt = createdAt; return this; }
        public WalletResponse build() { return response; }
    }
}
