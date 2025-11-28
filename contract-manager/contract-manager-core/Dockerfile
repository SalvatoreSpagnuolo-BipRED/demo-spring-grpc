# Multi-stage build for contract-manager-core
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy entire project (simpler and more reliable than selective copying)
COPY . .

# Build the application
RUN mvn clean package -DskipTests -pl contract-manager/contract-manager-core -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/contract-manager/contract-manager-core/target/contract-manager-core-*.jar app.jar

# Expose gRPC port
EXPOSE 9091

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
