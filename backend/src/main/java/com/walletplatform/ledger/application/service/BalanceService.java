package com.walletplatform.ledger.application.service;

import com.walletplatform.ledger.infrastructure.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public BalanceService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(UUID walletId) {
        BigDecimal credits = ledgerEntryRepository.sumWalletCredits(walletId);
        BigDecimal debits = ledgerEntryRepository.sumWalletDebits(walletId);
        
        if (credits == null) credits = BigDecimal.ZERO;
        if (debits == null) debits = BigDecimal.ZERO;
        
        return credits.subtract(debits);
    }

    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(UUID walletId, BigDecimal amount) {
        BigDecimal balance = calculateBalance(walletId);
        return balance.compareTo(amount) >= 0;
    }

    @Transactional(readOnly = true)
    public BalanceInfo getBalanceWithVerification(UUID walletId, BigDecimal cachedBalance) {
        BigDecimal ledgerBalance = calculateBalance(walletId);
        boolean consistent = ledgerBalance.compareTo(cachedBalance) == 0;
        
        return new BalanceInfo(walletId, ledgerBalance, cachedBalance, consistent);
    }

    public record BalanceInfo(
        UUID walletId,
        BigDecimal ledgerBalance,
        BigDecimal cachedBalance,
        boolean consistent
    ) {}
}
