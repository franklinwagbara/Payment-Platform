package com.walletplatform.transaction.application;

import com.walletplatform.transaction.application.service.TransactionService;
import com.walletplatform.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionFacade {

    private final TransactionService transactionService;

    public TransactionFacade(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public Page<Transaction> getTransactionsByUser(UUID userId, Pageable pageable) {
        return transactionService.getTransactionsByUser(userId, pageable);
    }

    public Page<Transaction> getTransactionsByWallet(UUID walletId, Pageable pageable) {
        return transactionService.getTransactionsByWallet(walletId, pageable);
    }
}

