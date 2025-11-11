package ru.maksm.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для ответа операции с кошельком
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationResponse {
    
    private UUID walletId;
    private BigDecimal balance;
    private String message;
}
