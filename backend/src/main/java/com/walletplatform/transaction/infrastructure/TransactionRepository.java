package com.walletplatform.transaction.infrastructure;

import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.transaction.domain.TransactionStatus;
import com.walletplatform.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletId(@Param("walletId") UUID walletId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.owner.id = :userId OR t.targetWallet.owner.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId) AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndDateRange(
        @Param("walletId") UUID walletId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Analytics queries
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sourceWallet.id = :walletId AND t.type = :type AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumByWalletAndTypeAndDateRange(
        @Param("walletId") UUID walletId,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sourceWallet.id = :walletId AND t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countByWalletAndStatusAndDateRange(
        @Param("walletId") UUID walletId,
        @Param("status") TransactionStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Monthly spending analytics
    @Query(value = """
        SELECT 
            EXTRACT(YEAR FROM t.created_at) as year,
            EXTRACT(MONTH FROM t.created_at) as month,
            SUM(t.amount) as total_amount,
            COUNT(t.id) as transaction_count
        FROM transactions t 
        WHERE t.source_wallet_id = :walletId 
            AND t.status = 'COMPLETED'
            AND t.created_at >= :startDate
        GROUP BY EXTRACT(YEAR FROM t.created_at), EXTRACT(MONTH FROM t.created_at)
        ORDER BY year DESC, month DESC
        """, nativeQuery = true)
    List<Object[]> getMonthlySpendingAnalytics(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate);
    
    long countByStatus(TransactionStatus status);
    
    long countByCreatedAtAfter(LocalDateTime dateTime);
}
