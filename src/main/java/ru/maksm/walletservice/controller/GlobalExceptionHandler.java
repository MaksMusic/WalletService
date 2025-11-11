package ru.maksm.walletservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений
 * обеспечивает единообразную обработку ошибок API
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Обработка ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("error", "Ошибка валидации");
        response.put("message", "Неверные данные в запросе");
        response.put("details", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        log.warn("Ошибка валидации: {}", errors);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Обработка ошибок парсинга JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseException(
            HttpMessageNotReadableException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Ошибка парсинга JSON");
        response.put("message", "Неверный формат JSON в теле запроса");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        log.warn("Ошибка парсинга JSON: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Обработка ошибок неверного типа параметра (например, неверный UUID)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Неверный тип параметра");
        response.put("message", "Параметр '" + ex.getName() + "' имеет неверный формат");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        log.warn("Неверный тип параметра {}: {}", ex.getName(), ex.getValue());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Обработка общих исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Внутренняя ошибка сервера");
        response.put("message", "Произошла неожиданная ошибка");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        log.error("Необработанное исключение: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
