# Dockerfile pour DirectoryService (Eureka Server)
# Fichier: DirectoryService/Dockerfile

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copier le pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copier le code source
COPY src ./src

# Build du projet
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# Copier le JAR depuis le stage de build
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Utiliser l'utilisateur non-root
USER spring:spring

# Variables d'environnement
# ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8761
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

EXPOSE 8761

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

