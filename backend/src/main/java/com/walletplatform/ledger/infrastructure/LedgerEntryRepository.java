package com.walletplatform.ledger.infrastructure;

import com.walletplatform.ledger.domain.AccountType;
import com.walletplatform.ledger.domain.EntryType;
import com.walletplatform.ledger.domain.LedgerEntry;
import com.walletplatform.wallet.domain.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    /**
     * Find all ledger entries for a specific wallet
     */
    Page<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    /**
     * Find all ledger entries for a specific transaction
     */
    List<LedgerEntry> findByTransactionId(UUID transactionId);

    /**
     * Sum all debits for a specific currency
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.entryType = 'DEBIT' AND e.currency = :currency")
    BigDecimal sumDebitsByCurrency(Currency currency);

    /**
     * Sum all credits for a specific currency
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.entryType = 'CREDIT' AND e.currency = :currency")
    BigDecimal sumCreditsByCurrency(Currency currency);

    /**
     * Sum all wallet debits for a specific wallet
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.wallet.id = :walletId AND e.entryType = 'DEBIT'")
    BigDecimal sumWalletDebits(UUID walletId);

    /**
     * Sum all wallet credits for a specific wallet
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.wallet.id = :walletId AND e.entryType = 'CREDIT'")
    BigDecimal sumWalletCredits(UUID walletId);

    /**
     * Count entries by entry type for verification
     */
    long countByEntryType(EntryType entryType);

    /**
     * Count entries by account type
     */
    long countByAccountType(AccountType accountType);
}
