package com.walletplatform.shared.mapper;

import com.walletplatform.shared.dto.TransactionResponse;
import com.walletplatform.shared.dto.UserResponse;
import com.walletplatform.shared.dto.WalletResponse;
import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.identity.domain.User;
import com.walletplatform.wallet.domain.Wallet;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    public WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .currency(wallet.getCurrency().name())
                .currencySymbol(wallet.getCurrency().getSymbol())
                .balance(wallet.getBalance())
                .dailyLimit(wallet.getDailyLimit())
                .spentToday(wallet.getSpentToday())
                .remainingDailyLimit(wallet.getRemainingDailyLimit())
                .active(wallet.isActive())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    public WalletResponse toWalletResponse(Wallet wallet, java.math.BigDecimal ledgerBalance) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .currency(wallet.getCurrency().name())
                .currencySymbol(wallet.getCurrency().getSymbol())
                .balance(ledgerBalance)  // Use ledger-derived balance
                .dailyLimit(wallet.getDailyLimit())
                .spentToday(wallet.getSpentToday())
                .remainingDailyLimit(wallet.getRemainingDailyLimit())
                .active(wallet.isActive())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .sourceWalletId(transaction.getSourceWallet() != null ? transaction.getSourceWallet().getId() : null)
                .targetWalletId(transaction.getTargetWallet() != null ? transaction.getTargetWallet().getId() : null)
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .sourceCurrency(transaction.getSourceCurrency().name())
                .convertedAmount(transaction.getConvertedAmount())
                .targetCurrency(transaction.getTargetCurrency() != null ? transaction.getTargetCurrency().name() : null)
                .exchangeRate(transaction.getExchangeRate())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }
}
