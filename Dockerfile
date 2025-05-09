FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем всё и собираем проект
COPY . .
RUN mvn clean package -DskipTests

# Финальный образ для запуска
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Устанавливаем Python и pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

# Копируем JAR-файл и Python-скрипты
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/src/main/resources/scripts/forecast.py ./forecast.py

# Устанавливаем Python-зависимости (если есть)
COPY requirements.txt .
RUN pip3 install -r requirements.txt

# Запуск Spring Boot-приложения
CMD ["java", "-jar", "app.jar"]
