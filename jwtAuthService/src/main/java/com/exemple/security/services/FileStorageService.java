package com.exemple.security.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
	
	
	
	private final Path uploadDir = Paths.get("uploads"); // 🔥 Dossier uploads dans le projet

    public FileStorageService() {
        try {
            // 🔥 Vérifie et crée le dossier `uploads` si nécessaire
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", e);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        // 🔥 Générer un nom de fichier unique
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        // 🔥 Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName; // 🔥 Retourner le chemin du fichier
    }
    
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        } else {
            throw new IOException("Fichier introuvable : " + filePath);
        }
    }
}