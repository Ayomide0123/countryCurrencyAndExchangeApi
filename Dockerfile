# ---------- STAGE 1: build ----------
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy only what we need to speed up builds
COPY pom.xml mvnw ./
COPY .mvn .mvn
# download dependencies (cache)
RUN ./mvnw -q -B dependency:go-offline

# copy source and build
COPY src ./src
RUN ./mvnw -q -B package -DskipTests

# ---------- STAGE 2: runtime ----------
# Use a Debian-based Temurin JDK so apt-get is available
FROM eclipse-temurin:21-jdk

# Install native libs needed for Java font rendering / image generation
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      libfreetype6 \
      libfreetype6-dev \
      fontconfig \
      libxext6 \
      libxrender1 \
      libxtst6 \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy jar from builder
COPY --from=builder /workspace/target/*.jar app.jar

# Create a non-root user (optional but recommended)
RUN useradd --create-home appuser && chown -R appuser:appuser /app
USER appuser

# Expose port (Railway provides $PORT env, but expose default)
EXPOSE 8080

# Runtime options: pass JVM args if needed
ENV JAVA_OPTS=""

# Start
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
