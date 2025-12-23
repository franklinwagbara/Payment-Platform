package com.walletplatform.ledger.application;

import com.walletplatform.ledger.application.service.BalanceService;
import com.walletplatform.ledger.application.service.LedgerService;
import com.walletplatform.ledger.domain.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ledger module facade - delegates to LedgerService.
 * This provides module-level entry point while maintaining backwards compatibility.
 */
@Service
public class LedgerFacade {

    private final LedgerService ledgerService;
    private final BalanceService balanceService;

    public LedgerFacade(LedgerService ledgerService, BalanceService balanceService) {
        this.ledgerService = ledgerService;
        this.balanceService = balanceService;
    }

    public Page<LedgerEntry> getWalletLedger(UUID walletId, Pageable pageable) {
        return ledgerService.getWalletLedger(walletId, pageable);
    }

    public List<LedgerEntry> getTransactionEntries(UUID transactionId) {
        return ledgerService.getTransactionEntries(transactionId);
    }

    public Map<String, Object> verifyAllBalances() {
        return ledgerService.verifyAllBalances();
    }

    public java.math.BigDecimal calculateBalance(UUID walletId) {
        return balanceService.calculateBalance(walletId);
    }
}

