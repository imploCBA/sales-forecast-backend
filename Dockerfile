# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml и загружаем зависимости
COPY pom.xml ./
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline

# Копируем остальные файлы проекта
COPY . ./

# Сборка проекта
RUN mvn clean package -DskipTests

# Запускаем приложение
CMD ["java", "-jar", "target/sales-forecast-backend-0.0.1-SNAPSHOT.jar"]