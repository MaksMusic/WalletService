package ru.maksm.walletservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность кошелька
 * содержит информацию о балансе и метаданные
 */
@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_id", columnList = "wallet_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "wallet_id", unique = true, nullable = false)
    private UUID walletId;
    
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "version", nullable = false)
    @Version
    // оптимистическая версия как страховка: если когда то появятся апдейты без явной блоки
    // то конкурирующее сохранение с устаревшей версией упадет и мы это обработаем
    private Long version;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public Wallet(UUID walletId) {
        this.walletId = walletId;
        this.balance = BigDecimal.ZERO;
    }
}
