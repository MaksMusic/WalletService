package ru.maksm.walletservice.exception;

/**
 * Исключение при отсутствии кошелька
 */
public class WalletNotFoundException extends RuntimeException {
    
    public WalletNotFoundException(String message) {
        super(message);
    }
    
    public WalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
