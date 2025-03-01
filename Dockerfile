
# Stage 1: Build APP (jar)
FROM maven:3.8.7-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn package -DskipTests

# Stage 2: Create image
FROM amazoncorretto:17.0.14-alpine3.21

WORKDIR /app

# Copy file JAR từ bước build vào container
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

COPY .env /app/.env

ENTRYPOINT ["java", "-jar", "app.jar"]
