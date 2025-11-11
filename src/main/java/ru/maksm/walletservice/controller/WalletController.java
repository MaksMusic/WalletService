package ru.maksm.walletservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maksm.walletservice.dto.WalletBalanceResponse;
import ru.maksm.walletservice.dto.WalletOperationRequest;
import ru.maksm.walletservice.dto.WalletOperationResponse;
import ru.maksm.walletservice.exception.InsufficientFundsException;
import ru.maksm.walletservice.exception.WalletNotFoundException;
import ru.maksm.walletservice.service.WalletService;

import java.util.UUID;

/**
 * REST контроллер для работы с кошельками
 * предоставляет API для операций с кошельками и получения баланса
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * Выполнить операцию с кошельком (пополнение или списание)
     * 
     * @param request запрос с данными операции
     * @return результат операции
     */
    @PostMapping("/wallet")
    public ResponseEntity<WalletOperationResponse> performOperation(
            @Valid @RequestBody WalletOperationRequest request) {
        
        log.info("Получен запрос на операцию {} для кошелька {} на сумму {}", 
                request.getOperationType(), request.getWalletId(), request.getAmount());
        
        try {
            WalletOperationResponse response = walletService.performOperation(request);
            return ResponseEntity.ok(response);
        } catch (WalletNotFoundException e) {
            log.warn("Кошелек не найден: {}", request.getWalletId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new WalletOperationResponse(request.getWalletId(), null, e.getMessage()));
        } catch (InsufficientFundsException e) {
            log.warn("Недостаточно средств для операции: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new WalletOperationResponse(request.getWalletId(), null, e.getMessage()));
        } catch (Exception e) {
            log.error("Ошибка при выполнении операции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WalletOperationResponse(request.getWalletId(), null, "Внутренняя ошибка сервера"));
        }
    }
    
    /**
     * Получить баланс кошелька
     * 
     * @param walletId ID кошелька
     * @return баланс кошелька
     */
    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable UUID walletId) {
        
        log.info("Получен запрос на баланс кошелька: {}", walletId);
        
        try {
            WalletBalanceResponse response = walletService.getBalance(walletId);
            return ResponseEntity.ok(response);
        } catch (WalletNotFoundException e) {
            log.warn("Кошелек не найден: {}", walletId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ошибка при получении баланса: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
