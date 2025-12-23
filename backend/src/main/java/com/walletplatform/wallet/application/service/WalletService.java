package com.walletplatform.wallet.application.service;

import com.walletplatform.shared.event.AuditEvent;
import com.walletplatform.shared.exception.DailyLimitExceededException;
import com.walletplatform.shared.exception.InsufficientFundsException;
import com.walletplatform.shared.exception.WalletNotFoundException;
import com.walletplatform.identity.domain.*;
import com.walletplatform.wallet.domain.*;
import com.walletplatform.transaction.domain.*;
import com.walletplatform.transaction.application.service.TransactionOrchestrator;
import com.walletplatform.transaction.application.service.TransactionOrchestrator.TransferCommand;
import com.walletplatform.transaction.application.service.TransactionOrchestrator.TransactionResult;
import com.walletplatform.transaction.infrastructure.TransactionRepository;
import com.walletplatform.wallet.infrastructure.WalletRepository;
import com.walletplatform.ledger.application.service.LedgerService;
import com.walletplatform.shared.event.TopUpCompletedEvent;
import com.walletplatform.shared.event.WithdrawalCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LedgerService ledgerService;
    private final TransactionOrchestrator transactionOrchestrator;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository,
                         ApplicationEventPublisher eventPublisher,
                         LedgerService ledgerService, TransactionOrchestrator transactionOrchestrator) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.ledgerService = ledgerService;
        this.transactionOrchestrator = transactionOrchestrator;
    }

    @Transactional
    public Wallet createWallet(User owner, Currency currency, String ipAddress) {
        if (walletRepository.existsByOwnerIdAndCurrency(owner.getId(), currency)) {
            throw new IllegalArgumentException("Wallet already exists for currency: " + currency);
        }

        Wallet wallet = Wallet.builder()
                .owner(owner)
                .currency(currency)
                .build();

        wallet = walletRepository.save(wallet);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                owner.getId(),
                "WALLET_CREATED",
                "Wallet",
                wallet.getId(),
                Map.of("currency", currency.name()),
                ipAddress
        ));

        return wallet;
    }

    @Transactional(readOnly = true)
    public List<Wallet> getUserWallets(UUID userId) {
        return walletRepository.findByOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
    }

    @Transactional(readOnly = true)
    public Wallet getWalletWithOwner(UUID walletId) {
        return walletRepository.findByIdWithOwner(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount, String description, String ipAddress) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        wallet.credit(amount);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .targetWallet(wallet)
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .sourceCurrency(wallet.getCurrency())
                .description(description != null ? description : "Top-up")
                .build();
        transaction.complete();

        transaction = transactionRepository.save(transaction);

        // Record ledger entries
        ledgerService.recordTopUp(transaction, wallet, amount);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                wallet.getOwner().getId(),
                "WALLET_TOP_UP",
                "Transaction",
                transaction.getId(),
                Map.of("walletId", walletId, "amount", amount, "currency", wallet.getCurrency().name()),
                ipAddress
        ));

        eventPublisher.publishEvent(new TopUpCompletedEvent(
                transaction.getId(),
                transaction.getId(),
                wallet.getId(),
                wallet.getOwner().getId(),
                amount,
                wallet.getCurrency().name()
        ));

        return transaction;
    }

    public Transaction transfer(UUID sourceWalletId, UUID targetWalletId, BigDecimal amount, 
                                 String description, String ipAddress) {
        return transfer(sourceWalletId, targetWalletId, amount, description, ipAddress, null);
    }

    @Transactional
    public Transaction transfer(UUID sourceWalletId, UUID targetWalletId, BigDecimal amount, 
                                 String description, String ipAddress, String idempotencyKey) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (sourceWalletId.equals(targetWalletId)) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        TransferCommand command = new TransferCommand(
            sourceWalletId,
            targetWalletId,
            amount,
            description,
            idempotencyKey
        );

        TransactionResult result = transactionOrchestrator.transfer(command);

        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + sourceWalletId));
        
        eventPublisher.publishEvent(new AuditEvent(
                this,
                sourceWallet.getOwner().getId(),
                "TRANSFER_COMPLETED",
                "Transaction",
                result.transactionId(),
                Map.of(
                        "sourceWalletId", sourceWalletId,
                        "targetWalletId", targetWalletId,
                        "amount", amount,
                        "convertedAmount", result.targetAmount()
                ),
                ipAddress
        ));

        return transactionRepository.findById(result.transactionId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found after creation"));
    }

    @Transactional
    public Wallet updateDailyLimit(UUID walletId, BigDecimal newLimit, UUID userId, String ipAddress) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        if (!wallet.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to update this wallet");
        }

        if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily limit cannot be negative");
        }

        wallet.setDailyLimit(newLimit);
        wallet = walletRepository.save(wallet);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                userId,
                "DAILY_LIMIT_UPDATED",
                "Wallet",
                walletId,
                Map.of("newLimit", newLimit, "currency", wallet.getCurrency().name()),
                ipAddress
        ));

        return wallet;
    }

    @Transactional
    public Transaction withdraw(UUID walletId, BigDecimal amount, String bankAccountNumber, 
                                 String bankName, String description, String ipAddress) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        // Check sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal");
        }

        // Check daily limit
        if (!wallet.canSpend(amount)) {
            throw new DailyLimitExceededException("Withdrawal would exceed daily limit");
        }

        wallet.debit(amount);
        walletRepository.save(wallet);

        String withdrawalDescription = description != null ? description : 
            String.format("Withdrawal to %s (%s)", bankName, maskBankAccount(bankAccountNumber));

        Transaction transaction = Transaction.builder()
                .sourceWallet(wallet)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .sourceCurrency(wallet.getCurrency())
                .description(withdrawalDescription)
                .build();
        transaction.complete();

        transaction = transactionRepository.save(transaction);

        // Record ledger entries
        ledgerService.recordWithdrawal(transaction, wallet, amount);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                wallet.getOwner().getId(),
                "WALLET_WITHDRAWAL",
                "Transaction",
                transaction.getId(),
                Map.of("walletId", walletId, "amount", amount, "currency", wallet.getCurrency().name(),
                       "bankName", bankName, "bankAccount", maskBankAccount(bankAccountNumber)),
                ipAddress
        ));

        eventPublisher.publishEvent(new WithdrawalCompletedEvent(
                transaction.getId(),
                transaction.getId(),
                wallet.getId(),
                wallet.getOwner().getId(),
                amount,
                wallet.getCurrency().name(),
                bankName
        ));

        return transaction;
    }

    private String maskBankAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
