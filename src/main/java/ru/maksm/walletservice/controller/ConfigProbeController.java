package ru.maksm.walletservice.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Простой эндпоинт для проверки загрузки конфигурации из Spring Cloud Config (Git)
 * Значение обновляется без рестарта после POST /actuator/refresh
 */
@RestController
@RequestMapping("/api/v1/config")
public class ConfigProbeController {

    private final Environment environment;

    public ConfigProbeController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/probe")
    public ResponseEntity<Integer> getProbeValue() {
        Integer value = environment.getProperty("wallet.probe.value", Integer.class, 0);
        return ResponseEntity.ok(value);
    }
}


