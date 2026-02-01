# Multi-stage build for Spring Boot application
# Stage 1: BUILD - Compile the application
FROM gradle:9.2.1-jdk17 AS builder

WORKDIR /app

# Copy all source code
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew bootJar --no-daemon

# Stage 2: RUNTIME - Run the application
FROM eclipse-temurin:17-jre

# Install netcat for healthcheck
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Change ownership to spring user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD nc -z localhost 8080 || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
