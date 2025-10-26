FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn

# 👇 Give permission to run the mvnw script
RUN chmod +x mvnw

# download dependencies (cache)
RUN ./mvnw -q -B dependency:go-offline

# copy source and build
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk

# 👇 Install missing dependencies (for fonts, etc.)
RUN apt-get update && apt-get install -y --no-install-recommends \
    libfreetype6 libfreetype6-dev fontconfig libxext6 libxrender1 libxtst6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
