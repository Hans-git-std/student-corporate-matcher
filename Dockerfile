# ==========================================
# Stage 1: Build & Package
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build executable JAR
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Stage 2: Ultra-Minimal Low-RAM JRE Runtime (<250MB RAM footprint)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root security user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from builder stage
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser:appgroup

# Optimized JVM flags for 500MB Host RAM:
# - Serial GC: Eliminates G1/Parallel worker thread stack overhead
# - TieredStopAtLevel=1: Light C1 JIT, saving ~60MB code cache
# - Metaspace capped at 128MB
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseSerialGC -Xms96m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/v1/ping || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
