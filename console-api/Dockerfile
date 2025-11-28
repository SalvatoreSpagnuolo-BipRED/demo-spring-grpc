# Multi-stage build for console-api
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy entire project (simpler and more reliable than selective copying)
COPY . .

# Build the application
RUN mvn clean package -DskipTests -pl console-api -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/console-api/target/console-api-*.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
