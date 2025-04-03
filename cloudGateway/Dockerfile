# Utiliser une image de base avec Java
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier JAR de ton projet dans l'image Docker
COPY target/cloudGateway-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port de l'application (par défaut, Spring Cloud Gateway utilise le port 8080)
EXPOSE 8088

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]