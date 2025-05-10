COPY requirements.txt .
RUN pip install -r requirements.txt

FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests


# --- Финальный образ ---
FROM eclipse-temurin:17-jdk-slim AS runtime
WORKDIR /app

# Устанавливаем только необходимые пакеты для Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv --no-install-recommends && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Установка зависимостей Python
RUN python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install -r requirements.txt && \
    rm -rf ~/.cache/pip

ENV PATH="/opt/venv/bin:$PATH"

# Копируем необходимые артефакты
COPY src/main/python/forecast.py /app/scripts/forecast.py
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
