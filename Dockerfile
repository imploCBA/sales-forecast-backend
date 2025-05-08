FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем всё и собираем проект
COPY . .
RUN mvn clean package -DskipTests

# Финальный образ для запуска
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Копируем jar-файл из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Запуск приложения
CMD ["java", "-jar", "app.jar"]