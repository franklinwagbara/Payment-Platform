package com.walletplatform.wallet.infrastructure;

import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.wallet.domain.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
    List<Wallet> findByOwnerId(UUID ownerId);
    
    Optional<Wallet> findByOwnerIdAndCurrency(UUID ownerId, Currency currency);
    
    boolean existsByOwnerIdAndCurrency(UUID ownerId, Currency currency);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") UUID id);
    
    @Query("SELECT w FROM Wallet w JOIN FETCH w.owner WHERE w.id = :id")
    Optional<Wallet> findByIdWithOwner(@Param("id") UUID id);
    
    @Query("SELECT SUM(w.balance) FROM Wallet w")
    BigDecimal sumAllBalances();
    
    @Query("SELECT w.currency, COUNT(w) FROM Wallet w GROUP BY w.currency")
    List<Object[]> countByCurrency();
}
