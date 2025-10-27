package com.exemple.security.services;

import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.HikingWaypoint;
import com.exemple.security.entities.Spot;
import com.exemple.security.repositories.HikingSpotRepository;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.tools.GoogleMapsUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PhotoRefreshService {

    @Autowired
    private SpotRepository spotRepository;
    
    @Autowired
    private HikingSpotRepository hikingSpotRepository;
    
    @Value("${google.maps.api.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // ========================================
    // 🔄 REFRESH AUTOMATIQUE MENSUEL
    // ========================================
    
    @Scheduled(cron = "0 0 3 1 * ?")
    public void autoRefreshPhotos() {
        System.out.println("\n🔄 === REFRESH AUTOMATIQUE MENSUEL ===");
        System.out.println("📅 Date: " + LocalDateTime.now());
        
        refreshAllSpots();
        refreshAllHikingSpots();
        
        System.out.println("=====================================\n");
    }

    // ========================================
    // 🔄 REFRESH SPOTS - PUBLIC
    // ========================================
    
    /**
     * 🔥 PUBLIC - Refresh d'un Spot spécifique
     */
    public Map<String, Object> refreshSpot(int spotId) {
        System.out.println("\n🔄 === REFRESH SPOT ===");
        System.out.println("📍 Spot ID: " + spotId);
        
        Optional<Spot> optionalSpot = spotRepository.findById(spotId);
        
        if (optionalSpot.isEmpty()) {
            return Map.of(
                "success", false,
                "message", "❌ Spot non trouvé."
            );
        }

        Spot spot = optionalSpot.get();

        if (spot.getPlaceId() == null || spot.getPlaceId().isEmpty()) {
            return Map.of(
                "success", false,
                "message", "⚠️ Ce spot n'a pas de placeId Google Maps."
            );
        }

        try {
            boolean refreshed = refreshSpotPhotos(spot);
            
            if (refreshed) {
                spotRepository.save(spot);
                System.out.println("✅ Spot rafraîchi avec succès\n");
                
                return Map.of(
                    "success", true,
                    "message", "✅ Photos rafraîchies avec succès"
                );
            } else {
                return Map.of(
                    "success", false,
                    "message", "❌ Impossible de rafraîchir les photos (aucune photo disponible)"
                );
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return Map.of(
                "success", false,
                "message", "❌ Erreur lors du refresh: " + e.getMessage()
            );
        }
    }

    /**
     * 🔥 PUBLIC - Refresh de tous les Spots
     */
    public Map<String, Object> refreshAllSpots() {
        System.out.println("\n📍 === REFRESH TOUS LES SPOTS ===");
        
        List<Spot> allSpots = spotRepository.findAll();
        
        if (allSpots.isEmpty()) {
            return Map.of(
                "success", true,
                "message", "ℹ️ Aucun Spot à rafraîchir.",
                "total_spots", 0
            );
        }

        int total = allSpots.size();
        int success = 0;
        int failed = 0;
        List<Map<String, Object>> spotResults = new ArrayList<>();

        for (Spot spot : allSpots) {
            if (spot.getPlaceId() != null && !spot.getPlaceId().isEmpty()) {
                try {
                    boolean refreshed = refreshSpotPhotos(spot);
                    if (refreshed) {
                        success++;
                        spotRepository.save(spot);
                        spotResults.add(Map.of(
                            "spotId", spot.getId(),
                            "spotName", spot.getName(),
                            "status", "success"
                        ));
                    } else {
                        failed++;
                        spotResults.add(Map.of(
                            "spotId", spot.getId(),
                            "spotName", spot.getName(),
                            "status", "failed",
                            "message", "Aucune photo disponible"
                        ));
                    }
                } catch (Exception e) {
                    failed++;
                    spotResults.add(Map.of(
                        "spotId", spot.getId(),
                        "spotName", spot.getName(),
                        "status", "error",
                        "message", e.getMessage()
                    ));
                    System.err.println("❌ Erreur spot " + spot.getId() + ": " + e.getMessage());
                }
            } else {
                failed++;
            }
        }

        System.out.println("📊 Spots - Total: " + total + " | ✅ Succès: " + success + " | ❌ Échecs: " + failed);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "✅ Refresh des Spots terminé");
        response.put("total_spots", total);
        response.put("success_spots", success);
        response.put("failed_spots", failed);
        response.put("spots_details", spotResults);

        return response;
    }

    /**
     * PRIVATE - Méthode interne pour refresh un Spot
     */
    private boolean refreshSpotPhotos(Spot spot) {
        try {
            System.out.println("  🔄 Refresh: " + spot.getName());
            
            String url = String.format(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
                spot.getPlaceId(),
                googleApiKey
            );
            
            GooglePlaceResponse response = restTemplate.getForObject(url, GooglePlaceResponse.class);
            
            if (response == null || !"OK".equals(response.getStatus())) {
                System.err.println("    ❌ API Status: " + (response != null ? response.getStatus() : "null"));
                return false;
            }
            
            if (response.getResult() == null || response.getResult().getPhotos() == null) {
                System.out.println("    ⚠️ Aucune photo disponible");
                return false;
            }
            
            List<String> newPhotoReferences = new ArrayList<>();
            
            for (GooglePhoto photo : response.getResult().getPhotos()) {
                if (photo.getPhotoReference() != null && !photo.getPhotoReference().isEmpty()) {
                    newPhotoReferences.add(photo.getPhotoReference());
                }
            }
            
            if (newPhotoReferences.isEmpty()) {
                return false;
            }
            
            // Garder les fichiers locaux
            List<String> updatedImages = new ArrayList<>();
            if (spot.getImageUrls() != null) {
                for (String img : spot.getImageUrls()) {
                    if (!GoogleMapsUtils.isGooglePhotoReference(img)) {
                        updatedImages.add(img);
                    }
                }
            }
            
            updatedImages.addAll(newPhotoReferences);
            
            spot.setImageUrls(updatedImages);
            spot.setImagePath(updatedImages.isEmpty() ? null : updatedImages.get(0));
            spot.setLastPhotoRefresh(LocalDateTime.now());
            
            System.out.println("    ✅ " + newPhotoReferences.size() + " photos rafraîchies");
            return true;
            
        } catch (Exception e) {
            System.err.println("    ❌ Exception: " + e.getMessage());
            return false;
        }
    }

    // ========================================
    // 🔄 REFRESH HIKING SPOTS - PUBLIC
    // ========================================
    
    /**
     * 🔥 PUBLIC - Refresh d'un HikingSpot spécifique
     */
    public Map<String, Object> refreshHikingSpot(int hikingSpotId) {
        System.out.println("\n🔄 === REFRESH HIKING SPOT ===");
        System.out.println("🏞️ HikingSpot ID: " + hikingSpotId);
        
        Optional<HikingSpot> optionalHikingSpot = hikingSpotRepository.findById(hikingSpotId);
        
        if (optionalHikingSpot.isEmpty()) {
            return Map.of(
                "success", false,
                "message", "❌ HikingSpot non trouvé."
            );
        }

        HikingSpot hikingSpot = optionalHikingSpot.get();

        if (hikingSpot.getWaypoints() == null || hikingSpot.getWaypoints().isEmpty()) {
            return Map.of(
                "success", false,
                "message", "⚠️ Aucun waypoint à rafraîchir pour ce HikingSpot."
            );
        }

        int totalWaypoints = hikingSpot.getWaypoints().size();
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (HikingWaypoint waypoint : hikingSpot.getWaypoints()) {
            if (waypoint.getPlaceId() != null && !waypoint.getPlaceId().isEmpty()) {
                try {
                    boolean refreshed = refreshWaypointPhotos(waypoint);
                    if (refreshed) {
                        successCount++;
                    } else {
                        failedCount++;
                        errors.add(waypoint.getName() + ": Aucune photo disponible");
                    }
                } catch (Exception e) {
                    failedCount++;
                    errors.add(waypoint.getName() + ": " + e.getMessage());
                }
            } else {
                failedCount++;
                errors.add(waypoint.getName() + ": Pas de placeId");
            }
        }

        hikingSpotRepository.save(hikingSpot);

        System.out.println("📊 Waypoints - Total: " + totalWaypoints + " | ✅ Succès: " + successCount + " | ❌ Échecs: " + failedCount);
        System.out.println("===================\n");

        Map<String, Object> response = new HashMap<>();
        response.put("success", successCount > 0);
        response.put("total", totalWaypoints);
        response.put("success_count", successCount);
        response.put("failed_count", failedCount);
        
        if (successCount == totalWaypoints) {
            response.put("message", "✅ Toutes les photos ont été rafraîchies (" + successCount + "/" + totalWaypoints + ")");
        } else if (successCount > 0) {
            response.put("message", "⚠️ Photos partiellement rafraîchies (" + successCount + "/" + totalWaypoints + ")");
            response.put("errors", errors);
        } else {
            response.put("message", "❌ Impossible de rafraîchir les photos");
            response.put("errors", errors);
        }

        return response;
    }

    /**
     * 🔥 PUBLIC - Refresh de tous les HikingSpots
     */
    public Map<String, Object> refreshAllHikingSpots() {
        System.out.println("\n🏞️ === REFRESH TOUS LES HIKING SPOTS ===");
        
        List<HikingSpot> allHikingSpots = hikingSpotRepository.findAll();
        
        if (allHikingSpots.isEmpty()) {
            return Map.of(
                "success", true,
                "message", "ℹ️ Aucun HikingSpot à rafraîchir.",
                "total_spots", 0
            );
        }

        int totalSpots = allHikingSpots.size();
        int totalWaypoints = 0;
        int successWaypoints = 0;
        int failedWaypoints = 0;
        List<Map<String, Object>> spotResults = new ArrayList<>();

        for (HikingSpot hikingSpot : allHikingSpots) {
            if (hikingSpot.getWaypoints() == null || hikingSpot.getWaypoints().isEmpty()) {
                spotResults.add(Map.of(
                    "spotId", hikingSpot.getId(),
                    "spotName", hikingSpot.getName(),
                    "status", "skipped",
                    "message", "Aucun waypoint"
                ));
                continue;
            }

            int spotSuccess = 0;
            int spotFailed = 0;

            for (HikingWaypoint waypoint : hikingSpot.getWaypoints()) {
                totalWaypoints++;
                
                if (waypoint.getPlaceId() != null && !waypoint.getPlaceId().isEmpty()) {
                    try {
                        boolean refreshed = refreshWaypointPhotos(waypoint);
                        if (refreshed) {
                            successWaypoints++;
                            spotSuccess++;
                        } else {
                            failedWaypoints++;
                            spotFailed++;
                        }
                    } catch (Exception e) {
                        failedWaypoints++;
                        spotFailed++;
                    }
                } else {
                    failedWaypoints++;
                    spotFailed++;
                }
            }

            hikingSpotRepository.save(hikingSpot);

            spotResults.add(Map.of(
                "spotId", hikingSpot.getId(),
                "spotName", hikingSpot.getName(),
                "waypointsTotal", hikingSpot.getWaypoints().size(),
                "waypointsSuccess", spotSuccess,
                "waypointsFailed", spotFailed
            ));
        }

        System.out.println("📊 HikingSpots - Total: " + totalSpots);
        System.out.println("📊 Waypoints - Total: " + totalWaypoints + " | ✅ Succès: " + successWaypoints + " | ❌ Échecs: " + failedWaypoints);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "✅ Refresh des HikingSpots terminé");
        response.put("total_spots", totalSpots);
        response.put("total_waypoints", totalWaypoints);
        response.put("success_waypoints", successWaypoints);
        response.put("failed_waypoints", failedWaypoints);
        response.put("spots_details", spotResults);

        return response;
    }

    /**
     * PRIVATE - Méthode interne pour refresh un waypoint
     */
    private boolean refreshWaypointPhotos(HikingWaypoint waypoint) {
        try {
            System.out.println("  🔄 Refresh: " + waypoint.getName());
            
            String url = String.format(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
                waypoint.getPlaceId(),
                googleApiKey
            );
            
            GooglePlaceResponse response = restTemplate.getForObject(url, GooglePlaceResponse.class);
            
            if (response == null || !"OK".equals(response.getStatus())) {
                System.err.println("    ❌ API Status: " + (response != null ? response.getStatus() : "null"));
                return false;
            }
            
            if (response.getResult() == null || response.getResult().getPhotos() == null) {
                System.out.println("    ⚠️ Aucune photo disponible");
                return false;
            }
            
            List<String> newPhotoReferences = new ArrayList<>();
            
            for (GooglePhoto photo : response.getResult().getPhotos()) {
                if (photo.getPhotoReference() != null && !photo.getPhotoReference().isEmpty()) {
                    newPhotoReferences.add(photo.getPhotoReference());
                }
            }
            
            if (newPhotoReferences.isEmpty()) {
                return false;
            }
            
            waypoint.setPhotos(newPhotoReferences);
            waypoint.setLastPhotoRefresh(LocalDateTime.now());
            
            System.out.println("    ✅ " + newPhotoReferences.size() + " photos rafraîchies");
            return true;
            
        } catch (Exception e) {
            System.err.println("    ❌ Exception: " + e.getMessage());
            return false;
        }
    }
}

// 📦 Classes pour parser la réponse Google Places API
class GooglePlaceResponse {
    private String status;
    private GooglePlaceResult result;
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public GooglePlaceResult getResult() { return result; }
    public void setResult(GooglePlaceResult result) { this.result = result; }
}

class GooglePlaceResult {
    private List<GooglePhoto> photos;
    
    public List<GooglePhoto> getPhotos() { return photos; }
    public void setPhotos(List<GooglePhoto> photos) { this.photos = photos; }
}

class GooglePhoto {
    @JsonProperty("photo_reference")
    private String photoReference;
    
    public String getPhotoReference() { return photoReference; }
    public void setPhotoReference(String photoReference) { this.photoReference = photoReference; }
}