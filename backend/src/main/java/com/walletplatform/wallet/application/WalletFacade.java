package com.walletplatform.wallet.application;

import com.walletplatform.wallet.application.service.WalletService;
import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.identity.domain.User;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.wallet.domain.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletFacade {

    private final WalletService walletService;

    public WalletFacade(WalletService walletService) {
        this.walletService = walletService;
    }

    public Wallet createWallet(User owner, Currency currency, String ipAddress) {
        return walletService.createWallet(owner, currency, ipAddress);
    }

    public List<Wallet> getUserWallets(UUID userId) {
        return walletService.getUserWallets(userId);
    }

    public Wallet getWalletWithOwner(UUID walletId) {
        return walletService.getWalletWithOwner(walletId);
    }

    public Transaction topUp(UUID walletId, BigDecimal amount, String description, String ipAddress) {
        return walletService.topUp(walletId, amount, description, ipAddress);
    }

    public Transaction transfer(UUID sourceWalletId, UUID targetWalletId, BigDecimal amount,
                                String description, String ipAddress) {
        return walletService.transfer(sourceWalletId, targetWalletId, amount, description, ipAddress);
    }

    public Transaction withdraw(UUID walletId, BigDecimal amount, String bankName,
                                String bankAccountNumber, String description, String ipAddress) {
        return walletService.withdraw(walletId, amount, bankName, bankAccountNumber, description, ipAddress);
    }
}
