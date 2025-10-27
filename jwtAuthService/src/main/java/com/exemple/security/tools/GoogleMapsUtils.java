package com.exemple.security.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleMapsUtils {
    
    @Value("${google.maps.api.key}")
    private String googleApiKey;
    
    private static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo";
    
    /**
     * Vérifie si c'est une photo_reference Google
     * (longue string sans "/" ni ".")
     */
    public static boolean isGooglePhotoReference(String path) {
        return path != null 
            && path.length() > 100 
            && !path.contains("/") 
            && !path.contains(".");
    }
    
    /**
     * Construit l'URL complète d'une photo Google
     */
    public String buildPhotoUrl(String photoReference, int maxWidth) {
        return String.format("%s?photoreference=%s&maxwidth=%d&key=%s",
            PHOTO_URL, photoReference, maxWidth, googleApiKey);
    }
    
    /**
     * Génère l'URL appropriée selon le type d'image
     */
    public String getImageUrl(String imagePath, String baseUrl) {
        if (isGooglePhotoReference(imagePath)) {
            return buildPhotoUrl(imagePath, 800);
        }
        return baseUrl + "/api/files/" + imagePath;
    }
}

    /**
     * Extrait l'extension du fichier depuis l'URL
     */
    /* 
    private String getExtensionFromUrl(String url) {
        String extension = "";

        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < url.length() - 1) {
            extension = url.substring(lastDotIndex + 1);
            // Nettoyer les paramètres éventuels après l'extension
            int paramIndex = extension.indexOf('?');
            if (paramIndex != -1) {
                extension = extension.substring(0, paramIndex);
            }
        }

        // Limiter à des extensions courantes
        if (extension.matches("(?i)jpg|jpeg|png|gif|bmp")) {
            return extension;
        } else {
            return "jpg"; // Valeur par défaut
        }
    }*/