package com.walletplatform.transaction.domain;

import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.wallet.domain.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_source", columnList = "source_wallet_id"),
    @Index(name = "idx_transaction_target", columnList = "target_wallet_id"),
    @Index(name = "idx_transaction_created", columnList = "created_at")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_wallet_id")
    private Wallet targetWallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency sourceCurrency;

    @Column(precision = 19, scale = 2)
    private BigDecimal convertedAmount;

    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    @Column(precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public Transaction() {}

    public UUID getId() { return id; }
    public Wallet getSourceWallet() { return sourceWallet; }
    public Wallet getTargetWallet() { return targetWallet; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public Currency getSourceCurrency() { return sourceCurrency; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public Currency getTargetCurrency() { return targetCurrency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public String getDescription() { return description; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setId(UUID id) { this.id = id; }
    public void setSourceWallet(Wallet sourceWallet) { this.sourceWallet = sourceWallet; }
    public void setTargetWallet(Wallet targetWallet) { this.targetWallet = targetWallet; }
    public void setType(TransactionType type) { this.type = type; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setSourceCurrency(Currency sourceCurrency) { this.sourceCurrency = sourceCurrency; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }
    public void setTargetCurrency(Currency targetCurrency) { this.targetCurrency = targetCurrency; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public void setDescription(String description) { this.description = description; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public void complete() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        this.status = TransactionStatus.CANCELLED;
    }

    public boolean isCrossCurrency() {
        return targetCurrency != null && !sourceCurrency.equals(targetCurrency);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Wallet sourceWallet;
        private Wallet targetWallet;
        private TransactionType type;
        private TransactionStatus status = TransactionStatus.PENDING;
        private BigDecimal amount;
        private Currency sourceCurrency;
        private BigDecimal convertedAmount;
        private Currency targetCurrency;
        private BigDecimal exchangeRate;
        private String description;

        public Builder sourceWallet(Wallet sourceWallet) { this.sourceWallet = sourceWallet; return this; }
        public Builder targetWallet(Wallet targetWallet) { this.targetWallet = targetWallet; return this; }
        public Builder type(TransactionType type) { this.type = type; return this; }
        public Builder status(TransactionStatus status) { this.status = status; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder sourceCurrency(Currency sourceCurrency) { this.sourceCurrency = sourceCurrency; return this; }
        public Builder convertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; return this; }
        public Builder targetCurrency(Currency targetCurrency) { this.targetCurrency = targetCurrency; return this; }
        public Builder exchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; return this; }
        public Builder description(String description) { this.description = description; return this; }

        public Transaction build() {
            Transaction t = new Transaction();
            t.sourceWallet = this.sourceWallet;
            t.targetWallet = this.targetWallet;
            t.type = this.type;
            t.status = this.status;
            t.amount = this.amount;
            t.sourceCurrency = this.sourceCurrency;
            t.convertedAmount = this.convertedAmount;
            t.targetCurrency = this.targetCurrency;
            t.exchangeRate = this.exchangeRate;
            t.description = this.description;
            return t;
        }
    }
}
