# ===== Builder stage =====
FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /workspace

# Копируем всю структуру проекта (мульти-модуль)
COPY . .

# Собираем только основной сервис
RUN ./gradlew --no-daemon :bootJar

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем jar из builder
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# Непривилегированный пользователь
RUN addgroup -S wallet && adduser -S wallet -G wallet
USER wallet

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
