# Multi-stage build for customer-manager-core
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy entire project (simpler and more reliable than selective copying)
COPY . .

# Build the application
RUN mvn clean package -DskipTests -pl customer-manager/customer-manager-core -am

# Runtime stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/customer-manager/customer-manager-core/target/customer-manager-core-*.jar app.jar

# Expose gRPC port
EXPOSE 9092

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
