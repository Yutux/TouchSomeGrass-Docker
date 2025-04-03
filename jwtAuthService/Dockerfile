# Utiliser une image de base avec Java
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier JAR de ton projet dans l'image Docker
COPY target/security-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port de l'application (par défaut, l'AuthenticationService utilise le port 8081)
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
