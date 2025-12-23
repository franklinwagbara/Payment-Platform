package com.walletplatform.ledger.application.service;

import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.ledger.domain.LedgerEntry;
import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.ledger.infrastructure.LedgerEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing double-entry ledger operations.
 * Ensures all financial transactions create balanced debit/credit entries.
 */
@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    /**
     * Create ledger entries for a top-up operation.
     * DEBIT: SYSTEM_CASH (cash received)
     * CREDIT: WALLET (wallet funded)
     */
    @Transactional
    public List<LedgerEntry> recordTopUp(Transaction transaction, Wallet wallet, BigDecimal amount) {
        List<LedgerEntry> entries = List.of(
            LedgerEntry.debitSystemCash(transaction, amount, wallet.getCurrency(), "Cash received for top-up"),
            LedgerEntry.creditWallet(transaction, wallet, amount, "Wallet funded")
        );
        return ledgerEntryRepository.saveAll(entries);
    }

    /**
     * Create ledger entries for a withdrawal operation.
     * DEBIT: WALLET (funds removed)
     * CREDIT: SYSTEM_CASH (cash paid out)
     */
    @Transactional
    public List<LedgerEntry> recordWithdrawal(Transaction transaction, Wallet wallet, BigDecimal amount) {
        List<LedgerEntry> entries = List.of(
            LedgerEntry.debitWallet(transaction, wallet, amount, "Withdrawal"),
            LedgerEntry.creditSystemCash(transaction, amount, wallet.getCurrency(), "Cash paid out")
        );
        return ledgerEntryRepository.saveAll(entries);
    }

    /**
     * Create ledger entries for a same-currency transfer.
     * DEBIT: Source WALLET
     * CREDIT: Target WALLET
     */
    @Transactional
    public List<LedgerEntry> recordSameCurrencyTransfer(Transaction transaction, 
                                                         Wallet sourceWallet, 
                                                         Wallet targetWallet, 
                                                         BigDecimal amount) {
        List<LedgerEntry> entries = List.of(
            LedgerEntry.debitWallet(transaction, sourceWallet, amount, "Transfer out"),
            LedgerEntry.creditWallet(transaction, targetWallet, amount, "Transfer in")
        );
        return ledgerEntryRepository.saveAll(entries);
    }

    /**
     * Create ledger entries for a cross-currency transfer.
     * Uses EXCHANGE account as suspense for currency conversion.
     * 
     * DEBIT: Source WALLET (source amount)
     * CREDIT: EXCHANGE (source amount in source currency)
     * DEBIT: EXCHANGE (target amount in target currency)
     * CREDIT: Target WALLET (target amount)
     */
    @Transactional
    public List<LedgerEntry> recordCrossCurrencyTransfer(Transaction transaction,
                                                          Wallet sourceWallet,
                                                          Wallet targetWallet,
                                                          BigDecimal sourceAmount,
                                                          BigDecimal targetAmount) {
        List<LedgerEntry> entries = List.of(
            LedgerEntry.debitWallet(transaction, sourceWallet, sourceAmount, "Transfer out (FX)"),
            LedgerEntry.creditExchange(transaction, sourceAmount, sourceWallet.getCurrency(), "FX: received source currency"),
            LedgerEntry.debitExchange(transaction, targetAmount, targetWallet.getCurrency(), "FX: released target currency"),
            LedgerEntry.creditWallet(transaction, targetWallet, targetAmount, "Transfer in (FX)")
        );
        return ledgerEntryRepository.saveAll(entries);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntry> getWalletLedger(UUID walletId, Pageable pageable) {
        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getTransactionEntries(UUID transactionId) {
        return ledgerEntryRepository.findByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateWalletBalanceFromLedger(UUID walletId) {
        BigDecimal credits = ledgerEntryRepository.sumWalletCredits(walletId);
        BigDecimal debits = ledgerEntryRepository.sumWalletDebits(walletId);
        return credits.subtract(debits);
    }

    @Transactional(readOnly = true)
    public boolean verifySystemBalance(Currency currency) {
        BigDecimal totalDebits = ledgerEntryRepository.sumDebitsByCurrency(currency);
        BigDecimal totalCredits = ledgerEntryRepository.sumCreditsByCurrency(currency);
        return totalDebits.compareTo(totalCredits) == 0;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verifyAllBalances() {
        Map<String, Object> result = new HashMap<>();
        boolean allBalanced = true;

        for (Currency currency : Currency.values()) {
            BigDecimal debits = ledgerEntryRepository.sumDebitsByCurrency(currency);
            BigDecimal credits = ledgerEntryRepository.sumCreditsByCurrency(currency);
            boolean balanced = debits.compareTo(credits) == 0;
            
            Map<String, Object> currencyStatus = new HashMap<>();
            currencyStatus.put("totalDebits", debits);
            currencyStatus.put("totalCredits", credits);
            currencyStatus.put("balanced", balanced);
            
            result.put(currency.name(), currencyStatus);
            if (!balanced) allBalanced = false;
        }

        result.put("allBalanced", allBalanced);
        result.put("entryCount", ledgerEntryRepository.count());
        
        return result;
    }
}
