package com.walletplatform.transaction.application.service;

import com.walletplatform.ledger.application.service.LedgerService;
import com.walletplatform.shared.exception.DailyLimitExceededException;
import com.walletplatform.shared.exception.InsufficientFundsException;
import com.walletplatform.shared.exception.WalletNotFoundException;
import com.walletplatform.wallet.domain.*;
import com.walletplatform.transaction.domain.*;
import com.walletplatform.shared.config.ExchangeRateService;
import com.walletplatform.transaction.infrastructure.TransactionRepository;
import com.walletplatform.wallet.infrastructure.WalletRepository;
import com.walletplatform.ledger.application.service.BalanceService;
import com.walletplatform.shared.event.DomainEventPublisher;
import com.walletplatform.shared.event.TransferCompletedEvent;
import com.walletplatform.shared.infrastructure.IdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Orchestrates financial transactions with ledger-first operations.
 * Ensures atomic, idempotent, and consistent financial flows.
 */
@Service
public class TransactionOrchestrator {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final BalanceService balanceService;
    private final ExchangeRateService exchangeRateService;
    private final DomainEventPublisher eventPublisher;
    private final IdempotencyService idempotencyService;

    public TransactionOrchestrator(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            LedgerService ledgerService,
            BalanceService balanceService,
            ExchangeRateService exchangeRateService,
            DomainEventPublisher eventPublisher,
            IdempotencyService idempotencyService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerService = ledgerService;
        this.balanceService = balanceService;
        this.exchangeRateService = exchangeRateService;
        this.eventPublisher = eventPublisher;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Execute a transfer with full ledger-first processing.
     * Idempotent: duplicate requests with same key return cached result.
     */
    @Transactional
    public TransactionResult transfer(TransferCommand command) {
        // Idempotency check - return cached result if already processed
        if (command.idempotencyKey() != null) {
            return idempotencyService.executeIdempotent(
                command.idempotencyKey(),
                TransactionResult.class,
                () -> executeTransfer(command)
            );
        }
        
        return executeTransfer(command);
    }

    private TransactionResult executeTransfer(TransferCommand command) {
        // Lock wallets in consistent order (prevent deadlocks)
        UUID firstId = command.sourceWalletId().compareTo(command.targetWalletId()) < 0 
            ? command.sourceWalletId() : command.targetWalletId();
        UUID secondId = command.sourceWalletId().compareTo(command.targetWalletId()) < 0 
            ? command.targetWalletId() : command.sourceWalletId();
        
        Wallet first = walletRepository.findByIdWithLock(firstId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + firstId));
        Wallet second = walletRepository.findByIdWithLock(secondId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + secondId));
        
        Wallet sourceWallet = first.getId().equals(command.sourceWalletId()) ? first : second;
        Wallet targetWallet = first.getId().equals(command.targetWalletId()) ? first : second;

        // Validate using ledger balance 
        BigDecimal sourceBalance = balanceService.calculateBalance(sourceWallet.getId());
        if (sourceBalance.compareTo(command.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        // Check daily limit
        if (!sourceWallet.canSpend(command.amount())) {
            throw new DailyLimitExceededException("Daily limit exceeded");
        }

        // Calculate converted amount for cross-currency
        BigDecimal convertedAmount = command.amount();
        BigDecimal exchangeRate = BigDecimal.ONE;
        if (!sourceWallet.getCurrency().equals(targetWallet.getCurrency())) {
            exchangeRate = exchangeRateService.getExchangeRate(
                sourceWallet.getCurrency(), targetWallet.getCurrency());
            convertedAmount = exchangeRateService.convert(
                command.amount(), sourceWallet.getCurrency(), targetWallet.getCurrency());
        }

        // Create transaction record
        Transaction transaction = Transaction.builder()
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.COMPLETED)
            .amount(command.amount())
            .sourceCurrency(sourceWallet.getCurrency())
            .convertedAmount(convertedAmount)
            .targetCurrency(targetWallet.getCurrency())
            .exchangeRate(exchangeRate)
            .description(command.description() != null ? command.description() : "Transfer")
            .build();
        transaction.complete();
        transaction = transactionRepository.save(transaction);

        // Record ledger entries
        if (sourceWallet.getCurrency().equals(targetWallet.getCurrency())) {
            ledgerService.recordSameCurrencyTransfer(transaction, sourceWallet, targetWallet, command.amount());
        } else {
            ledgerService.recordCrossCurrencyTransfer(transaction, sourceWallet, targetWallet, 
                command.amount(), convertedAmount);
        }

        // Update daily spending
        sourceWallet.recordSpending(command.amount());
        walletRepository.save(sourceWallet);

        // Sync cached balances
        sourceWallet.refreshBalanceFromLedger(balanceService.calculateBalance(sourceWallet.getId()));
        targetWallet.refreshBalanceFromLedger(balanceService.calculateBalance(targetWallet.getId()));
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        UUID correlationId = UUID.randomUUID();
        eventPublisher.publishAsync(new TransferCompletedEvent(
            correlationId,
            transaction.getId(),
            sourceWallet.getId(),
            targetWallet.getId(),
            command.amount(),
            convertedAmount,
            sourceWallet.getCurrency().name(),
            targetWallet.getCurrency().name()
        ));

        return new TransactionResult(
            transaction.getId(),
            TransactionStatus.COMPLETED,
            command.amount(),
            convertedAmount
        );
    }

    public record TransferCommand(
        UUID sourceWalletId,
        UUID targetWalletId,
        BigDecimal amount,
        String description,
        String idempotencyKey
    ) {}

    public record TransactionResult(
        UUID transactionId,
        TransactionStatus status,
        BigDecimal sourceAmount,
        BigDecimal targetAmount
    ) {}
}
