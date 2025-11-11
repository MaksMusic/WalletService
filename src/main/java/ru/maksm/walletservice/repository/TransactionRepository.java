package ru.maksm.walletservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.maksm.walletservice.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с транзакциями
 * предоставляет методы для получения истории операций
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    /**
     * Получить все транзакции для кошелька с пагинацией
     */
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);
    
    /**
     * Получить транзакции за период
     */
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndDateRange(@Param("walletId") UUID walletId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Получить последние N транзакций для кошелька
     */
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findTop10ByWalletIdOrderByCreatedAtDesc(@Param("walletId") UUID walletId, Pageable pageable);
}
