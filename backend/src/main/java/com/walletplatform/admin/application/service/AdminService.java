package com.walletplatform.admin.application.service;

import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.transaction.domain.TransactionStatus;
import com.walletplatform.identity.domain.User;
import com.walletplatform.identity.domain.UserRole;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.transaction.infrastructure.TransactionRepository;
import com.walletplatform.identity.infrastructure.UserRepository;
import com.walletplatform.wallet.infrastructure.WalletRepository;
import com.walletplatform.ledger.application.service.BalanceService;
import com.walletplatform.ledger.application.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BalanceService balanceService;
    private final LedgerService ledgerService;

    public AdminService(UserRepository userRepository, WalletRepository walletRepository,
                        TransactionRepository transactionRepository, BalanceService balanceService,
                        LedgerService ledgerService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.balanceService = balanceService;
        this.ledgerService = ledgerService;
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<Wallet> getUserWallets(UUID userId) {
        return walletRepository.findByOwnerId(userId);
    }

    @Transactional
    public Optional<User> updateUserRole(UUID userId, String role) {
        return userRepository.findById(userId)
            .map(user -> {
                UserRole newRole = UserRole.valueOf(role.toUpperCase());
                user.setRole(newRole);
                return userRepository.save(user);
            });
    }

    @Transactional(readOnly = true)
    public Page<Wallet> getAllWallets(Pageable pageable) {
        return walletRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // User statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        analytics.put("totalUsers", totalUsers);
        analytics.put("activeUsers", activeUsers);
        
        // Wallet statistics
        long totalWallets = walletRepository.count();
        BigDecimal totalBalance = walletRepository.sumAllBalances();
        analytics.put("totalWallets", totalWallets);
        analytics.put("totalBalanceAllWallets", totalBalance != null ? totalBalance : BigDecimal.ZERO);
        
        // Transaction statistics
        long totalTransactions = transactionRepository.count();
        long completedTransactions = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long failedTransactions = transactionRepository.countByStatus(TransactionStatus.FAILED);
        analytics.put("totalTransactions", totalTransactions);
        analytics.put("completedTransactions", completedTransactions);
        analytics.put("failedTransactions", failedTransactions);
        
        // Recent activity (last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long recentTransactions = transactionRepository.countByCreatedAtAfter(last24Hours);
        analytics.put("transactionsLast24Hours", recentTransactions);
        
        // Wallet distribution by currency
        List<Object[]> currencyDistribution = walletRepository.countByCurrency();
        Map<String, Long> currencyStats = currencyDistribution.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
        analytics.put("walletsByCurrency", currencyStats);
        
        return analytics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verifyAllBalances() {
        List<Wallet> allWallets = walletRepository.findAll();
        List<Map<String, Object>> discrepancies = new ArrayList<>();
        List<Map<String, Object>> verified = new ArrayList<>();
        
        for (Wallet wallet : allWallets) {
            BigDecimal cachedBalance = wallet.getBalance();
            BigDecimal ledgerBalance = balanceService.calculateBalance(wallet.getId());
            boolean consistent = cachedBalance.compareTo(ledgerBalance) == 0;
            
            Map<String, Object> walletStatus = new HashMap<>();
            walletStatus.put("walletId", wallet.getId());
            walletStatus.put("ownerId", wallet.getOwner().getId());
            walletStatus.put("currency", wallet.getCurrency().name());
            walletStatus.put("cachedBalance", cachedBalance);
            walletStatus.put("ledgerBalance", ledgerBalance);
            walletStatus.put("consistent", consistent);
            
            if (consistent) {
                verified.add(walletStatus);
            } else {
                walletStatus.put("discrepancy", cachedBalance.subtract(ledgerBalance));
                discrepancies.add(walletStatus);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalWallets", allWallets.size());
        result.put("consistentCount", verified.size());
        result.put("discrepancyCount", discrepancies.size());
        result.put("allConsistent", discrepancies.isEmpty());
        result.put("discrepancies", discrepancies);
        
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verifyLedgerIntegrity() {
        return ledgerService.verifyAllBalances();
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> verifyWalletBalance(UUID walletId) {
        return walletRepository.findById(walletId)
            .map(wallet -> {
                BigDecimal cachedBalance = wallet.getBalance();
                BigDecimal ledgerBalance = balanceService.calculateBalance(walletId);
                boolean consistent = cachedBalance.compareTo(ledgerBalance) == 0;
                
                Map<String, Object> result = new HashMap<>();
                result.put("walletId", walletId);
                result.put("ownerId", wallet.getOwner().getId());
                result.put("currency", wallet.getCurrency().name());
                result.put("cachedBalance", cachedBalance);
                result.put("ledgerBalance", ledgerBalance);
                result.put("consistent", consistent);
                
                if (!consistent) {
                    result.put("discrepancy", cachedBalance.subtract(ledgerBalance));
                    result.put("recommendation", "Run reconciliation to sync cached balance with ledger");
                }
                
                return result;
            });
    }

    @Transactional
    public Optional<Map<String, Object>> reconcileWalletBalance(UUID walletId) {
        return walletRepository.findById(walletId)
            .map(wallet -> {
                BigDecimal oldCachedBalance = wallet.getBalance();
                BigDecimal ledgerBalance = balanceService.calculateBalance(walletId);
                
                wallet.refreshBalanceFromLedger(ledgerBalance);
                walletRepository.save(wallet);
                
                Map<String, Object> result = new HashMap<>();
                result.put("walletId", walletId);
                result.put("previousCachedBalance", oldCachedBalance);
                result.put("newBalance", ledgerBalance);
                result.put("reconciled", true);
                
                return result;
            });
    }
}
