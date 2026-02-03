package com.exemple.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service

public class FileStorageService {

    private final Path uploadDir;
    private final CloudinaryService cloudinaryService;
    private final boolean useCloudinary;

    /**
     * Constructeur avec détection automatique de l'environnement
     * - Cloudinary : stockage persistant (production)
     * - Local : /tmp/uploads ou uploads (développement)
     */
    @Autowired
    public FileStorageService(
            /*@Value("${file.upload-dir}") String uploadDirPath,
            @Value("${cloudinary.enabled}") boolean useCloudinary,
            CloudinaryService cloudinaryService*/
            @Value("${file.upload-dir:uploads}") String uploadDirPath,
            @Value("${cloudinary.enabled:false}") boolean useCloudinary,
            Optional<CloudinaryService> cloudinaryService
    ) {
        this.cloudinaryService = cloudinaryService.orElse(null);
        this.useCloudinary = useCloudinary;

        // Configuration du dossier local (fallback)
        if (uploadDirPath == null || uploadDirPath.isBlank()) {
            uploadDirPath = System.getenv("RENDER") != null ? "/tmp/uploads" : "uploads";
        }
        
        this.uploadDir = Paths.get(uploadDirPath);
        
        try {
            if (!Files.exists(this.uploadDir)) {
                Files.createDirectories(this.uploadDir);
                System.out.println("✅ Dossier d'upload créé : " + this.uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("⚠️ Impossible de créer le dossier local : " + uploadDirPath);
        }

        if (useCloudinary) {
            System.out.println("☁️ Cloudinary activé - Stockage persistant sur le cloud");
        } else {
            System.out.println("💾 Stockage local activé - Fichiers temporaires");
        }
    }

    /**
     * Sauvegarde un fichier uploadé
     * - Si Cloudinary activé : upload sur Cloudinary (persistant)
     * - Sinon : sauvegarde locale (temporaire sur Render)
     * 
     * @param file Le fichier multipart à sauvegarder
     * @return URL Cloudinary ou nom de fichier local
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Le fichier est vide ou null");
        }

        // Utiliser Cloudinary si activé (priorité)
        /*if (useCloudinary) {
            try {
                String cloudinaryUrl = cloudinaryService.uploadFile(file);
                System.out.println("☁️ Fichier uploadé sur Cloudinary : " + cloudinaryUrl);
                return cloudinaryUrl;
            } catch (Exception e) {
                System.err.println("⚠️ Erreur Cloudinary, fallback vers stockage local : " + e.getMessage());
                // Fallback vers stockage local
            }
        }*/
       if (useCloudinary && cloudinaryService != null) {
            try {
                String cloudinaryUrl = cloudinaryService.uploadFile(file);
                return cloudinaryUrl;
            } catch (Exception e) {
                System.err.println("⚠️ Cloudinary échoué, fallback local : " + e.getMessage());
            }
        }

        // Stockage local (fallback ou si Cloudinary désactivé)
        return saveFileLocally(file);
    }

    /**
     * Sauvegarde locale d'un fichier (utilisé comme fallback)
     */
    private String saveFileLocally(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "file";
        }
        
        String fileName = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadDir.resolve(fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("💾 Fichier sauvegardé localement : " + filePath.toAbsolutePath());
        System.out.println("⚠️ Attention : Ce fichier sera perdu au redémarrage sur Render");
        return fileName;
    }

    /**
     * ⚠️ IMPORTANT : Google Maps interdit le téléchargement d'images
     * Cette méthode ne doit PAS être utilisée pour Google Maps
     * 
     * Pour Google Maps :
     * - Stocker uniquement la photo_reference dans la base
     * - Construire l'URL dynamiquement avec GoogleMapsUtils.buildPhotoUrl()
     * 
     * @deprecated Ne pas utiliser pour Google Maps (violation des CGU)
     */
    @Deprecated
    public String saveImageFromUrl(String imageUrl) {
        System.err.println("⚠️ WARNING : saveImageFromUrl() ne doit PAS être utilisé pour Google Maps !");
        System.err.println("⚠️ Stockez uniquement la photo_reference et construisez l'URL dynamiquement");
        return null;
    }

    /**
     * Supprime un fichier
     * - Si URL Cloudinary : supprime de Cloudinary
     * - Sinon : supprime localement
     * 
     * @param filePathOrUrl Nom de fichier local ou URL Cloudinary
     */
    public void deleteFile(String filePathOrUrl) throws IOException {
        if (filePathOrUrl == null || filePathOrUrl.isEmpty()) {
            throw new IOException("Chemin de fichier vide ou null");
        }

        // Si c'est une URL Cloudinary
        /*if (filePathOrUrl.contains("cloudinary.com")) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(filePathOrUrl);
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
                System.out.println("☁️ Image supprimée de Cloudinary : " + publicId);
                return;
            }
        }*/
       if (filePathOrUrl.contains("cloudinary.com") && cloudinaryService != null) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(filePathOrUrl);
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
                System.out.println("☁️ Image supprimée de Cloudinary : " + publicId);
                return;
            }
        }

        // Si c'est une URL Google Maps, ne rien faire
        if (filePathOrUrl.contains("maps.googleapis.com")) {
            System.out.println("ℹ️ URL Google Maps détectée, pas de suppression nécessaire");
            return;
        }

        // Sinon, suppression locale
        Path path = uploadDir.resolve(filePathOrUrl);
        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println("💾 Fichier local supprimé : " + path.toAbsolutePath());
        } else {
            throw new IOException("❌ Fichier introuvable : " + filePathOrUrl);
        }
    }

    /**
     * Obtient le chemin d'un fichier local
     * Note : Ne fonctionne que pour les fichiers locaux, pas les URLs Cloudinary
     */
    public Path getFilePath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Nom de fichier vide ou null");
        }
        
        // Si c'est une URL, on ne peut pas retourner de Path
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            throw new IllegalArgumentException("Impossible de créer un Path depuis une URL : " + fileName);
        }
        
        return uploadDir.resolve(fileName);
    }

    /**
     * Vérifie si un fichier existe localement
     * Note : Ne vérifie que les fichiers locaux
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        // Si c'est une URL, on considère qu'elle existe (pas de vérification)
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return true;
        }
        
        return Files.exists(uploadDir.resolve(fileName));
    }

    /**
     * Obtient le dossier d'upload actuel (local)
     */
    public Path getUploadDir() {
        return uploadDir;
    }

    /**
     * Vérifie si Cloudinary est activé
     */
    public boolean isCloudinaryEnabled() {
        return useCloudinary;
    }

    /**
     * Obtient une URL accessible pour un fichier
     * - Si c'est une URL complète (Cloudinary, Google Maps) : retourne telle quelle
     * - Si c'est un nom de fichier local : construit l'URL avec /api/files/
     */
    public String getAccessibleUrl(String filePathOrUrl, String baseUrl) {
        if (filePathOrUrl == null || filePathOrUrl.isEmpty()) {
            return null;
        }
        
        // Si c'est déjà une URL complète
        if (filePathOrUrl.startsWith("http://") || filePathOrUrl.startsWith("https://")) {
            return filePathOrUrl;
        }
        
        // Sinon, construire l'URL locale
        return baseUrl + "/api/files/" + filePathOrUrl;
    }
}