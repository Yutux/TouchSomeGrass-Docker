# ===============================
# Dockerfile pour jwtAuthService
# ===============================

# Stage 1 : Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 : Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Créer un utilisateur non-root
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar
USER spring:spring

# ✅ Variables d'environnement adaptées à Render
ENV SPRING_PROFILES_ACTIVE=postgres
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
# Passer la clé API Google Maps via une build-arg
ARG GOOGLE_MAPS_API_KEY
ARG CLOUDINARY_CLOUD_NAME
ARG CLOUDINARY_API_KEY
ARG CLOUDINARY_API_SECRET

ENV GOOGLE_MAPS_API_KEY=${GOOGLE_MAPS_API_KEY}
ENV CLOUDINARY_CLOUD_NAME=${CLOUDINARY_CLOUD_NAME}
ENV CLOUDINARY_API_KEY=${CLOUDINARY_API_KEY}
ENV CLOUDINARY_API_SECRET=${CLOUDINARY_API_SECRET}


# ✅ NE PAS fixer de port ici — Render injecte automatiquement $PORT
# (et ton Spring Boot lit déjà server.port=${PORT:8080})

# ✅ Healthcheck dynamique
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD wget --no-verbose --tries=1 --sp

# ❌ Pas de EXPOSE 8080
# Render détecte automatiquement le port via $PORT

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

