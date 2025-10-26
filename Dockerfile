# Stage 1: Build the application
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy Maven wrapper and settings
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Make mvnw executable (this fixes your error)
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw -q -B dependency:go-offline

# Copy the source code
COPY src src

# Build the app
RUN ./mvnw -q -B package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Expose port 8080 (Railway will map it automatically)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
