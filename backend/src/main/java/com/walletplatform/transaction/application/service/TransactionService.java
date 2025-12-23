package com.walletplatform.transaction.application.service;

import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.transaction.domain.TransactionStatus;
import com.walletplatform.transaction.domain.TransactionType;
import com.walletplatform.transaction.infrastructure.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByWallet(UUID walletId, Pageable pageable) {
        return transactionRepository.findByWalletId(walletId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByUser(UUID userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByWalletAndDateRange(UUID walletId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByWalletIdAndDateRange(walletId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWalletAnalytics(UUID walletId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalTransfers = transactionRepository.sumByWalletAndTypeAndDateRange(
                walletId, TransactionType.TRANSFER, startDate, endDate);
        
        Long completedCount = transactionRepository.countByWalletAndStatusAndDateRange(
                walletId, TransactionStatus.COMPLETED, startDate, endDate);
        
        Long failedCount = transactionRepository.countByWalletAndStatusAndDateRange(
                walletId, TransactionStatus.FAILED, startDate, endDate);

        return Map.of(
                "walletId", walletId,
                "period", Map.of("start", startDate, "end", endDate),
                "totalTransferred", totalTransfers != null ? totalTransfers : BigDecimal.ZERO,
                "completedTransactions", completedCount != null ? completedCount : 0,
                "failedTransactions", failedCount != null ? failedCount : 0
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlySpendingReport(UUID walletId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = transactionRepository.getMonthlySpendingAnalytics(walletId, startDate);
        
        return results.stream()
                .map(row -> Map.<String, Object>of(
                        "year", ((Number) row[0]).intValue(),
                        "month", ((Number) row[1]).intValue(),
                        "totalAmount", row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO,
                        "transactionCount", ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }
}
