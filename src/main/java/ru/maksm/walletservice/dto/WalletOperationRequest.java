package ru.maksm.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.maksm.walletservice.model.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для запроса операции с кошельком
 */
@Data
public class WalletOperationRequest {
    
    @NotNull(message = "Wallet ID не может быть пустым")
    private UUID walletId;
    
    @NotNull(message = "Тип операции не может быть пустым")
    private OperationType operationType;
    
    @NotNull(message = "Сумма не может быть пустой")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}
