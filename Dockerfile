# --- СТАДИЯ 1: Maven сборка jar-файла ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- СТАДИЯ 2: Финальный образ с Python и Java ---
FROM eclipse-temurin:17-jdk-slim AS runtime
WORKDIR /app

# Устанавливаем Python и необходимые зависимости
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        python3 python3-pip python3-venv \
        build-essential libopenblas-dev liblapack-dev gfortran && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Копируем requirements.txt
COPY requirements.txt .

# Устанавливаем Python-зависимости в виртуальное окружение
RUN python3 -m venv /opt/venv && \
    . /opt/venv/bin/activate && \
    pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

ENV PATH="/opt/venv/bin:$PATH"

# Копируем Python-скрипт
COPY src/main/python/forecast.py /app/scripts/forecast.py

# Копируем собранный jar-файл из стадии build
COPY --from=build /app/target/*.jar app.jar

# Команда запуска
CMD ["java", "-jar", "app.jar"]
