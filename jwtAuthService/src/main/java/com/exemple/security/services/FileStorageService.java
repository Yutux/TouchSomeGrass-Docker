package com.exemple.security.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDirPath) {
        // ✅ Si Render, utilise /tmp/uploads (variable d'env ou profil postgres)
        // ✅ Sinon, utilise le dossier local "uploads"
        if (uploadDirPath == null || uploadDirPath.isBlank()) {
            uploadDirPath = System.getenv("RENDER") != null ? "/tmp/uploads" : "uploads";
        }

        this.uploadDir = Paths.get(uploadDirPath);

        try {
            if (!Files.exists(this.uploadDir)) {
                Files.createDirectories(this.uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Impossible de créer le dossier d'upload : " + uploadDirPath, e);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
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
