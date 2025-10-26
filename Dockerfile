# ---- Build Stage ----
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
