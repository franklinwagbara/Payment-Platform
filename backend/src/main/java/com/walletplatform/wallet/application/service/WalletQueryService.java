package com.walletplatform.wallet.application.service;

import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.ledger.application.service.BalanceService;
import com.walletplatform.wallet.infrastructure.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Wallet query service using ledger-first balance calculation.
 * All balance reads are derived from ledger entries.
 */
@Service
public class WalletQueryService {

    private final WalletRepository walletRepository;
    private final BalanceService balanceService;

    public WalletQueryService(WalletRepository walletRepository, BalanceService balanceService) {
        this.walletRepository = walletRepository;
        this.balanceService = balanceService;
    }

    /**
     * Get wallet balance from ledger.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID walletId) {
        return balanceService.calculateBalance(walletId);
    }

    /**
     * Get wallet with ledger-derived balance.
     */
    @Transactional(readOnly = true)
    public WalletView getWalletView(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
        
        BigDecimal ledgerBalance = balanceService.calculateBalance(walletId);
        
        return new WalletView(
            wallet.getId(),
            wallet.getOwner().getId(),
            wallet.getCurrency(),
            ledgerBalance,          
            wallet.getDailyLimit(),
            wallet.getSpentToday(),
            wallet.getRemainingDailyLimit()
        );
    }

    /**
     * Check if wallet can spend amount (balance + daily limit check).
     */
    @Transactional(readOnly = true)
    public boolean canSpend(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
        
        if (!balanceService.hasSufficientBalance(walletId, amount)) {
            return false;
        }
        
        return wallet.canSpend(amount);
    }

    /**
     * Verify wallet balance consistency between ledger and cached.
     */
    @Transactional(readOnly = true)
    public BalanceService.BalanceInfo verifyBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
        
        return balanceService.getBalanceWithVerification(walletId, wallet.getBalance());
    }

    public record WalletView(
        UUID id,
        UUID ownerId,
        Currency currency,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal spentToday,
        BigDecimal remainingDailyLimit
    ) {}
}
