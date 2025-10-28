package com.exemple.security.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true");

        this.cloudinary = new Cloudinary(config);
        
        System.out.println("✅ Cloudinary initialisé avec cloud_name: " + cloudName);
    }

    /**
     * Upload un fichier MultipartFile vers Cloudinary
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Le fichier est vide ou null");
        }

        try {
            Map uploadParams = ObjectUtils.asMap(
                    "folder", "hiking-spots",
                    "resource_type", "auto",
                    "public_id", UUID.randomUUID().toString()
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            System.out.println("✅ Fichier uploadé sur Cloudinary : " + secureUrl);
            
            return secureUrl;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'upload sur Cloudinary: " + e.getMessage());
            throw new IOException("Erreur lors de l'upload sur Cloudinary", e);
        }
    }

    /**
     * Upload une image depuis une URL (ex: Google Maps)
     */
    public String uploadImageFromUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IOException("URL d'image vide ou null");
        }

        try {
            Map uploadParams = ObjectUtils.asMap(
                    "folder", "hiking-spots/google-maps",
                    "resource_type", "image",
                    "public_id", UUID.randomUUID().toString()
            );

            Map uploadResult = cloudinary.uploader().upload(imageUrl, uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            System.out.println("✅ Image uploadée depuis URL vers Cloudinary : " + secureUrl);
            
            return secureUrl;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'upload depuis URL: " + e.getMessage());
            throw new IOException("Erreur lors de l'upload depuis URL", e);
        }
    }

    /**
     * Supprime une image de Cloudinary
     */
    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            throw new IOException("Public ID vide ou null");
        }

        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            
            if ("ok".equals(resultStatus)) {
                System.out.println("✅ Image supprimée de Cloudinary : " + publicId);
            } else {
                System.err.println("⚠️ Résultat de suppression : " + resultStatus);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression: " + e.getMessage());
            throw new IOException("Erreur lors de la suppression", e);
        }
    }

    /**
     * Extrait le public_id depuis une URL Cloudinary
     */
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || !cloudinaryUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];
            String[] segments = afterUpload.split("/");
            
            int startIndex = (segments.length > 0 && segments[0].startsWith("v")) ? 1 : 0;
            
            StringBuilder publicId = new StringBuilder();
            for (int i = startIndex; i < segments.length; i++) {
                if (i > startIndex) {
                    publicId.append("/");
                }
                String segment = segments[i];
                if (i == segments.length - 1 && segment.contains(".")) {
                    segment = segment.substring(0, segment.lastIndexOf("."));
                }
                publicId.append(segment);
            }

            return publicId.toString();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'extraction du public_id: " + e.getMessage());
            return null;
        }
    }

    /**
     * Génère une URL de transformation d'image (resize, crop, etc.)
     */
    public String getTransformedImageUrl(String publicId, int width, int height) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(width)
                        .height(height)
                        .crop("fill")
                        .quality("auto")
                        .fetchFormat("auto"))
                .secure(true)
                .generate(publicId);
    }

    /**
     * Génère une miniature (thumbnail)
     */
    public String getThumbnailUrl(String publicId) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(200)
                        .height(200)
                        .crop("thumb")
                        .gravity("center")
                        .quality("auto"))
                .secure(true)
                .generate(publicId);
    }

    /**
     * Génère une URL optimisée pour le web
     */
    public String getOptimizedImageUrl(String publicId) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(800)
                        .crop("limit")
                        .quality("auto:good")
                        .fetchFormat("auto"))
                .secure(true)
                .generate(publicId);
    }

    /**
     * Génère une URL responsive (plusieurs tailles disponibles)
     */
    public String getResponsiveImageUrl(String publicId, int width) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(width)
                        .crop("scale")
                        .quality("auto")
                        .fetchFormat("auto"))
                .secure(true)
                .generate(publicId);
    }

    /**
     * Génère plusieurs URLs pour des tailles responsive
     * Retourne une Map avec les différentes tailles
     */
    public Map<String, String> getResponsiveImageUrls(String publicId) {
        Map<String, String> urls = new HashMap<>();
        
        // Petite (mobile)
        urls.put("small", cloudinary.url()
                .transformation(new Transformation().width(320).crop("scale").quality("auto"))
                .secure(true)
                .generate(publicId));
        
        // Moyenne (tablette)
        urls.put("medium", cloudinary.url()
                .transformation(new Transformation().width(768).crop("scale").quality("auto"))
                .secure(true)
                .generate(publicId));
        
        // Grande (desktop)
        urls.put("large", cloudinary.url()
                .transformation(new Transformation().width(1200).crop("scale").quality("auto"))
                .secure(true)
                .generate(publicId));
        
        // Extra large (HD)
        urls.put("xlarge", cloudinary.url()
                .transformation(new Transformation().width(1920).crop("scale").quality("auto"))
                .secure(true)
                .generate(publicId));
        
        return urls;
    }
}