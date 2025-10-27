package com.exemple.security.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Constructeur avec détection automatique de l'environnement
     * - Render : utilise /tmp/uploads (système de fichiers temporaire)
     * - Local : utilise le dossier configuré ou "uploads" par défaut
     */
    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDirPath) {
        // Détection automatique de l'environnement Render
        if (uploadDirPath == null || uploadDirPath.isBlank()) {
            uploadDirPath = System.getenv("RENDER") != null ? "/tmp/uploads" : "uploads";
        }
        
        this.uploadDir = Paths.get(uploadDirPath);
        
        try {
            if (!Files.exists(this.uploadDir)) {
                Files.createDirectories(this.uploadDir);
                System.out.println("✅ Dossier d'upload créé : " + this.uploadDir.toAbsolutePath());
            } else {
                System.out.println("✅ Dossier d'upload existant : " + this.uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Impossible de créer le dossier d'upload : " + uploadDirPath, e);
        }
    }

    /**
     * Sauvegarde un fichier uploadé depuis le formulaire
     * @param file Le fichier multipart à sauvegarder
     * @return Le nom du fichier sauvegardé
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Le fichier est vide ou null");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "file";
        }
        
        String fileName = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadDir.resolve(fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("✅ Fichier sauvegardé : " + filePath.toAbsolutePath());
        return fileName;
    }

    /**
     * Télécharge et sauvegarde une image depuis une URL (Google Maps, etc.)
     * Utilise RestTemplate pour de petites images
     * @param imageUrl L'URL de l'image à télécharger
     * @return Le nom du fichier sauvegardé ou null en cas d'erreur
     */
    public String saveImageFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            System.err.println("⚠️ URL d'image vide ou null");
            return null;
        }

        try {
            // Télécharger l'image depuis l'URL
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);

            if (imageBytes == null || imageBytes.length == 0) {
                System.err.println("❌ Impossible de télécharger l'image depuis l'URL: " + imageUrl);
                return null;
            }

            // Déterminer l'extension du fichier
            String extension = getExtensionFromUrl(imageUrl);
            String fileName = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(fileName);

            // Sauvegarder l'image
            Files.write(filePath, imageBytes);

            System.out.println("✅ Image téléchargée et sauvegardée : " + filePath.toAbsolutePath());
            return fileName;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du téléchargement de l'image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Alternative avec InputStream pour gérer de grandes images
     * Plus efficace en mémoire que saveImageFromUrl()
     * @param imageUrl L'URL de l'image à télécharger
     * @return Le nom du fichier sauvegardé
     */
    public String saveImageFromUrlStream(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IOException("URL d'image vide ou null");
        }

        try {
            URL url = new URL(imageUrl);
            String extension = getExtensionFromUrl(imageUrl);
            String fileName = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(fileName);

            try (InputStream in = url.openStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Image téléchargée (stream) : " + filePath.toAbsolutePath());
            return fileName;

        } catch (Exception e) {
            throw new IOException("Erreur lors du téléchargement de l'image: " + e.getMessage(), e);
        }
    }

    /**
     * Extrait l'extension du fichier depuis l'URL
     * @param url L'URL contenant potentiellement une extension
     * @return L'extension avec le point (ex: ".jpg")
     */
    private String getExtensionFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return ".jpg";
        }
        
        String lowerUrl = url.toLowerCase();
        
        if (lowerUrl.contains(".png")) {
            return ".png";
        } else if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) {
            return ".jpg";
        } else if (lowerUrl.contains(".webp")) {
            return ".webp";
        } else if (lowerUrl.contains(".gif")) {
            return ".gif";
        } else if (lowerUrl.contains(".bmp")) {
            return ".bmp";
        }
        
        // Extension par défaut pour Google Maps
        return ".jpg";
    }

    /**
     * Supprime un fichier par son nom
     * @param fileName Le nom du fichier à supprimer
     */
    public void deleteFile(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Nom de fichier vide ou null");
        }

        Path path = uploadDir.resolve(fileName);
        
        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println("✅ Fichier supprimé : " + path.toAbsolutePath());
        } else {
            throw new IOException("❌ Fichier introuvable : " + fileName);
        }
    }

    /**
     * Obtient le chemin complet d'un fichier
     * @param fileName Le nom du fichier
     * @return Le Path complet du fichier
     */
    public Path getFilePath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Nom de fichier vide ou null");
        }
        return uploadDir.resolve(fileName);
    }

    /**
     * Vérifie si un fichier existe
     * @param fileName Le nom du fichier
     * @return true si le fichier existe, false sinon
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        return Files.exists(uploadDir.resolve(fileName));
    }

    /**
     * Obtient le dossier d'upload actuel
     * @return Le Path du dossier d'upload
     */
    public Path getUploadDir() {
        return uploadDir;
    }
}