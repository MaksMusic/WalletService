# Примеры запросов для тестирования Wallet Service

## Создание кошелька (через пополнение)

```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "DEPOSIT",
    "amount": 1000.00
  }'
```

## Пополнение кошелька

```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "DEPOSIT",
    "amount": 500.50
  }'
```

## Списание с кошелька

```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "WITHDRAW",
    "amount": 200.00
  }'
```

## Получение баланса

```bash
curl -X GET http://localhost:8080/api/v1/wallets/550e8400-e29b-41d4-a716-446655440000
```

## Тест недостатка средств

```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "WITHDRAW",
    "amount": 10000.00
  }'
```

## Тест несуществующего кошелька

```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "00000000-0000-0000-0000-000000000000",
    "operationType": "DEPOSIT",
    "amount": 100.00
  }'
```

## Тест неверного JSON

```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "DEPOSIT",
    "amount": "invalid"
  }'
```

## Нагрузочное тестирование с Apache Bench

### Подготовка файла с запросом
```bash
echo '{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 10.00
}' > deposit.json
```

### Тест на 1000 RPS
```bash
ab -n 10000 -c 100 -p deposit.json -T application/json http://localhost:8080/api/v1/wallet
```

### Тест смешанных операций
```bash
# Создать файл для списания
echo '{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "WITHDRAW",
  "amount": 5.00
}' > withdraw.json

# Запустить параллельно пополнения и списания
ab -n 5000 -c 50 -p deposit.json -T application/json http://localhost:8080/api/v1/wallet &
ab -n 5000 -c 50 -p withdraw.json -T application/json http://localhost:8080/api/v1/wallet &
```

## Проверка здоровья сервиса

```bash
curl http://localhost:8080/actuator/health
```

## Просмотр метрик

```bash
curl http://localhost:8080/actuator/metrics
```

## Swagger UI

Открыть в браузере: http://localhost:8080/swagger-ui.html
