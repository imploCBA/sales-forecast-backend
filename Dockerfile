FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем всё и собираем проект
COPY . .
RUN mvn clean package -DskipTests

# Финальный образ для запуска
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Устанавливаем Python и зависимости
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv

# Копируем requirements.txt
COPY requirements.txt .

# Создаём и активируем виртуальное окружение, затем устанавливаем зависимости
RUN python3 -m venv /opt/venv && \
    . /opt/venv/bin/activate && \
    /opt/venv/bin/pip install --upgrade pip && \
    /opt/venv/bin/pip install -r requirements.txt


# Копируем JAR-файл и Python-скрипты
COPY src/main/python/forecast.py /app/scripts/forecast.py
COPY --from=build /app/target/*.jar app.jar

# Устанавливаем Python-зависимости (если есть)
COPY requirements.txt .
RUN pip3 install -r requirements.txt

# Запуск Spring Boot-приложения
CMD ["java", "-jar", "app.jar"]
