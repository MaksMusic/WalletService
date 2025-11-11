package ru.maksm.walletservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.maksm.walletservice.model.Wallet;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с кошельками
 * использует pessimistic locking для обеспечения консистентности при конкурентном доступе
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
    /**
     * Найти кошелек по walletId с блокировкой для записи
     * используется для предотвращения race conditions при одновременных операциях
     *
     * почему это ок на 1000 rps:
     * - тут явная row level блокировка PESSIMISTIC_WRITE, база сама упорядочит конкурирующие апдейты одной строки
     * - время удержания блокировки минимально, потому что мы делаем короткую транзакцию и индексы стоят
     * - так мы исключаем гонки при списании/пополнении и не даем уйти в минус даже при одновременных запросах
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId")
    Optional<Wallet> findByWalletIdWithLock(@Param("walletId") UUID walletId);
    
    /**
     * Найти кошелек по walletId без блокировки
     * используется для чтения баланса
     */
    Optional<Wallet> findByWalletId(UUID walletId);
    
    /**
     * Проверить существование кошелька
     */
    boolean existsByWalletId(UUID walletId);
}
