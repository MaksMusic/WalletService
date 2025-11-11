package ru.maksm.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для ответа с балансом кошелька
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {
    
    private UUID walletId;
    private BigDecimal balance;
}
