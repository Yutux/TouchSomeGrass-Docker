# Utiliser l'image OpenJDK légère
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier JAR généré dans le conteneur
COPY target/DirectoryService-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port Eureka (8761)
EXPOSE 8761

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
