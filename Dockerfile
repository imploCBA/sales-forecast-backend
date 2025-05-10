# --- СТАДИЯ 1: Maven сборка jar-файла ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- СТАДИЯ 2: Финальный образ с Python и Java ---
FROM eclipse-temurin:17-jdk AS runtime
WORKDIR /app

# Устанавливаем Python и pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv --no-install-recommends && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Копируем requirements.txt и ставим зависимости
COPY requirements.txt .
RUN python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install -r requirements.txt && \
    rm -rf ~/.cache/pip

ENV PATH="/opt/venv/bin:$PATH"

# Копируем Python-скрипт
COPY src/main/python/forecast.py /app/scripts/forecast.py

# Копируем собранный jar-файл из стадии build
COPY --from=build /app/target/*.jar app.jar

# Команда запуска
CMD ["java", "-jar", "app.jar"]
