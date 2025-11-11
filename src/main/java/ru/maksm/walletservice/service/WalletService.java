package ru.maksm.walletservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.maksm.walletservice.dto.WalletBalanceResponse;
import ru.maksm.walletservice.dto.WalletOperationRequest;
import ru.maksm.walletservice.dto.WalletOperationResponse;
import ru.maksm.walletservice.exception.InsufficientFundsException;
import ru.maksm.walletservice.exception.WalletNotFoundException;
import ru.maksm.walletservice.model.OperationType;
import ru.maksm.walletservice.model.Transaction;
import ru.maksm.walletservice.model.Wallet;
import ru.maksm.walletservice.repository.TransactionRepository;
import ru.maksm.walletservice.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сервис для работы с кошельками
 * реализует принципы SOLID и обеспечивает конкурентную безопасность
 *
 * как держим 1000 rps на один кошелек не теряя корректность:
 * - используем короткие транзакции внутри БД только на время пересчета баланса
 * - берем строку кошелька на PESSIMISTIC_WRITE (см репозиторий), база сама сериализует апдейты одной строки
 * - уровень изоляции READ_COMMITTED чтобы не раздувать блокировки, dirty read нет и этого достаточно
 * - таймаут транзакции чтоб не было вечных подвисаний при очереди, а приложение не уходило в 50х
 * - индекс по wallet_id уменьшает время поиска строки => меньше удержание блокировки => выше пропускная
 * - оптимистическая версия в сущности как доп защита если вдруг будем апдейтить вне явной блоки (на будущее)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * Выполнить операцию с кошельком
     * использует pessimistic locking для предотвращения race conditions
     *
     * примечание про настройки:
     * - isolation READ_COMMITTED тут ок потому что мы блокируем запись и читаем внутри транзакции
     * - timeout 30 сек чтобы очередь под высокой нагрузкой не превращалась в дедлок навсегда
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public WalletOperationResponse performOperation(WalletOperationRequest request) {
        log.debug("Выполнение операции {} для кошелька {} на сумму {}", 
                 request.getOperationType(), request.getWalletId(), request.getAmount());
        
        // получаем кошелек с блокировкой для записи
        // тут важный момент для конкурентности 1000 rps: блокируем ровно одну строку и максимально коротко
        Wallet wallet = walletRepository.findByWalletIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Кошелек не найден: " + request.getWalletId()));
        
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal newBalance;
        
        // выполняем операцию в зависимости от типа
        if (request.getOperationType() == OperationType.DEPOSIT) {
            newBalance = balanceBefore.add(request.getAmount());
            log.debug("Пополнение: {} + {} = {}", balanceBefore, request.getAmount(), newBalance);
        } else {
            // проверяем достаточность средств
            if (balanceBefore.compareTo(request.getAmount()) < 0) {
                log.warn("Недостаточно средств для списания. Баланс: {}, запрошено: {}", 
                        balanceBefore, request.getAmount());
                throw new InsufficientFundsException("Недостаточно средств. Баланс: " + balanceBefore);
            }
            newBalance = balanceBefore.subtract(request.getAmount());
            log.debug("Списание: {} - {} = {}", balanceBefore, request.getAmount(), newBalance);
        }
        
        // обновляем баланс
        // сохраняем сразу чтобы отпустить блокировку побыстрее после коммита
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);
        
        // создаем запись о транзакции
        Transaction transaction = new Transaction(
                request.getWalletId(),
                request.getOperationType(),
                request.getAmount(),
                balanceBefore,
                newBalance
        );
        transactionRepository.save(transaction);
        
        log.info("Операция {} выполнена успешно. Кошелек: {}, новый баланс: {}", 
                request.getOperationType(), request.getWalletId(), newBalance);
        
        return new WalletOperationResponse(
                request.getWalletId(),
                newBalance,
                "Операция выполнена успешно"
        );
    }
    
    /**
     * Получить баланс кошелька
     * использует обычное чтение без блокировки
     * 
     * @param walletId ID кошелька
     * @return баланс кошелька
     */
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        log.debug("Получение баланса для кошелька: {}", walletId);
        
        Wallet wallet = walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Кошелек не найден: " + walletId));
        
        log.debug("Баланс кошелька {}: {}", walletId, wallet.getBalance());
        
        return new WalletBalanceResponse(walletId, wallet.getBalance());
    }
    
    /**
     * Создать новый кошелек
     * 
     * @param walletId ID кошелька
     * @return созданный кошелек
     */
    @Transactional
    public Wallet createWallet(UUID walletId) {
        log.info("Создание нового кошелька: {}", walletId);
        
        if (walletRepository.existsByWalletId(walletId)) {
            throw new IllegalArgumentException("Кошелек уже существует: " + walletId);
        }
        
        Wallet wallet = new Wallet(walletId);
        wallet = walletRepository.save(wallet);
        
        log.info("Кошелек создан успешно: {}", walletId);
        return wallet;
    }
}
