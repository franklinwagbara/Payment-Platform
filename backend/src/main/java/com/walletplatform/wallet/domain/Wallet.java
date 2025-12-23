package com.walletplatform.wallet.domain;

import com.walletplatform.identity.domain.User;
import com.walletplatform.transaction.domain.Transaction;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wallets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"owner_id", "currency"})
})
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("10000.00");

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal spentToday = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate lastSpendingResetDate = LocalDate.now();

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL)
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "targetWallet", cascade = CascadeType.ALL)
    private List<Transaction> incomingTransactions = new ArrayList<>();

    public Wallet() {}

    // Getters
    public UUID getId() { return id; }
    public User getOwner() { return owner; }
    public Currency getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public BigDecimal getSpentToday() { return spentToday; }
    public LocalDate getLastSpendingResetDate() { return lastSpendingResetDate; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public List<Transaction> getOutgoingTransactions() { return outgoingTransactions; }
    public List<Transaction> getIncomingTransactions() { return incomingTransactions; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
    public void setSpentToday(BigDecimal spentToday) { this.spentToday = spentToday; }
    public void setLastSpendingResetDate(LocalDate lastSpendingResetDate) { this.lastSpendingResetDate = lastSpendingResetDate; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setVersion(Long version) { this.version = version; }

    public void resetDailySpendingIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastSpendingResetDate)) {
            spentToday = BigDecimal.ZERO;
            lastSpendingResetDate = today;
        }
    }

    public boolean canSpend(BigDecimal amount) {
        resetDailySpendingIfNeeded();
        return spentToday.add(amount).compareTo(dailyLimit) <= 0;
    }

    public void recordSpending(BigDecimal amount) {
        resetDailySpendingIfNeeded();
        spentToday = spentToday.add(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public BigDecimal getRemainingDailyLimit() {
        resetDailySpendingIfNeeded();
        return dailyLimit.subtract(spentToday);
    }

    /**
     * Refresh cached balance from ledger calculation.
     * Called after ledger entries are created to sync cached balance.
     */
    public void refreshBalanceFromLedger(BigDecimal ledgerBalance) {
        this.balance = ledgerBalance;
    }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User owner;
        private Currency currency;
        private BigDecimal balance = BigDecimal.ZERO;
        private BigDecimal dailyLimit = new BigDecimal("10000.00");

        public Builder owner(User owner) { this.owner = owner; return this; }
        public Builder currency(Currency currency) { this.currency = currency; return this; }
        public Builder balance(BigDecimal balance) { this.balance = balance; return this; }
        public Builder dailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; return this; }

        public Wallet build() {
            Wallet wallet = new Wallet();
            wallet.owner = this.owner;
            wallet.currency = this.currency;
            wallet.balance = this.balance;
            wallet.dailyLimit = this.dailyLimit;
            return wallet;
        }
    }
}
