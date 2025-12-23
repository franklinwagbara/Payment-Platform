package com.walletplatform.ledger.domain;

import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.wallet.domain.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single entry in the double-entry ledger.
 * Every financial transaction creates balanced DEBIT and CREDIT entries.
 * The sum of all DEBITs must equal the sum of all CREDITs for each currency.
 */
@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_wallet", columnList = "wallet_id"),
    @Index(name = "idx_ledger_transaction", columnList = "transaction_id"),
    @Index(name = "idx_ledger_account_type", columnList = "account_type")
})
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;  // NULL for system accounts (SYSTEM_CASH, EXCHANGE, FEE)

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LedgerEntry() {}

    private LedgerEntry(Transaction transaction, Wallet wallet, AccountType accountType,
                        EntryType entryType, BigDecimal amount, Currency currency, String description) {
        this.transaction = transaction;
        this.wallet = wallet;
        this.accountType = accountType;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public UUID getId() { return id; }
    public Transaction getTransaction() { return transaction; }
    public Wallet getWallet() { return wallet; }
    public AccountType getAccountType() { return accountType; }
    public EntryType getEntryType() { return entryType; }
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID id) { this.id = id; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public void setDescription(String description) { this.description = description; }

    public static LedgerEntry debitWallet(Transaction txn, Wallet wallet, BigDecimal amount, String description) {
        return new LedgerEntry(txn, wallet, AccountType.WALLET, EntryType.DEBIT, 
                               amount, wallet.getCurrency(), description);
    }

    public static LedgerEntry creditWallet(Transaction txn, Wallet wallet, BigDecimal amount, String description) {
        return new LedgerEntry(txn, wallet, AccountType.WALLET, EntryType.CREDIT, 
                               amount, wallet.getCurrency(), description);
    }

    public static LedgerEntry debitSystemCash(Transaction txn, BigDecimal amount, Currency currency, String description) {
        return new LedgerEntry(txn, null, AccountType.SYSTEM_CASH, EntryType.DEBIT, 
                               amount, currency, description);
    }

    public static LedgerEntry creditSystemCash(Transaction txn, BigDecimal amount, Currency currency, String description) {
        return new LedgerEntry(txn, null, AccountType.SYSTEM_CASH, EntryType.CREDIT, 
                               amount, currency, description);
    }

    public static LedgerEntry debitExchange(Transaction txn, BigDecimal amount, Currency currency, String description) {
        return new LedgerEntry(txn, null, AccountType.EXCHANGE, EntryType.DEBIT, 
                               amount, currency, description);
    }

    public static LedgerEntry creditExchange(Transaction txn, BigDecimal amount, Currency currency, String description) {
        return new LedgerEntry(txn, null, AccountType.EXCHANGE, EntryType.CREDIT, 
                               amount, currency, description);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Transaction transaction;
        private Wallet wallet;
        private AccountType accountType;
        private EntryType entryType;
        private BigDecimal amount;
        private Currency currency;
        private String description;

        public Builder transaction(Transaction transaction) { this.transaction = transaction; return this; }
        public Builder wallet(Wallet wallet) { this.wallet = wallet; return this; }
        public Builder accountType(AccountType accountType) { this.accountType = accountType; return this; }
        public Builder entryType(EntryType entryType) { this.entryType = entryType; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(Currency currency) { this.currency = currency; return this; }
        public Builder description(String description) { this.description = description; return this; }

        public LedgerEntry build() {
            return new LedgerEntry(transaction, wallet, accountType, entryType, amount, currency, description);
        }
    }
}
