package com.exemple.security.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exemple.security.dtos.SearchRequestDto;
import com.exemple.security.dtos.SearchResponseDto;
import com.exemple.security.dtos.SpotDto;
import com.exemple.security.dtos.HikingSpotDto;
import com.exemple.security.dtos.SearchMetadata;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.repositories.HikingSpotRepository;

@Service
public class SearchService {

    @Autowired
    private SpotRepository spotRepository;
    
    @Autowired
    private HikingSpotRepository hikingSpotRepository;

    /**
     * 🔍 Recherche de Spots
     */
    public SearchResponseDto<SpotDto> searchSpots(SearchRequestDto request) {
        /*System.out.println("🔍 === SEARCH SPOTS ===");
        System.out.println("Query: " + request.getQuery());
        System.out.println("Creator: " + request.getCreatorEmail());*/
        
        List<Spot> allSpots = spotRepository.findAll();
        List<Spot> filteredSpots = new ArrayList<>(allSpots);

        // 1️⃣ Filtre textuel (nom + description)
        if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            String query = request.getQuery().toLowerCase();
            filteredSpots = filteredSpots.stream()
                .filter(spot -> 
                    (spot.getName() != null && spot.getName().toLowerCase().contains(query)) ||
                    (spot.getDescription() != null && spot.getDescription().toLowerCase().contains(query))
                )
                .collect(Collectors.toList());
            System.out.println("  Après filtre textuel: " + filteredSpots.size());
        }

        // 2️⃣ Filtre par créateur
        if (request.getCreatorEmail() != null && !request.getCreatorEmail().trim().isEmpty()) {
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getCreator() != null && 
                    spot.getCreator().getEmail().equalsIgnoreCase(request.getCreatorEmail()))
                .collect(Collectors.toList());
            System.out.println("  Après filtre créateur: " + filteredSpots.size());
        }

        // 3️⃣ Filtre géographique (rayon)
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadius() != null) {
            final double lat = request.getLatitude();
            final double lon = request.getLongitude();
            final double radiusMeters = request.getRadius();
            
            filteredSpots = filteredSpots.stream()
                .filter(spot -> {
                    double distance = calculateDistance(lat, lon, spot.getLatitude(), spot.getLongitude());
                    return distance <= radiusMeters;
                })
                .collect(Collectors.toList());
            System.out.println("  Après filtre géographique: " + filteredSpots.size());
        }

        // 4️⃣ Tri
        filteredSpots = sortSpots(filteredSpots, request);

        // 5️⃣ Pagination
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        int totalResults = filteredSpots.size();
        int totalPages = (int) Math.ceil((double) totalResults / size);

        int start = page * size;
        int end = Math.min(start + size, totalResults);
        
        List<Spot> paginatedSpots = start < totalResults 
            ? filteredSpots.subList(start, end) 
            : new ArrayList<>();

        // 6️⃣ Conversion en DTO
        List<SpotDto> spotDtos = paginatedSpots.stream()
            .map(spot -> SpotDto.builder()
                .id(spot.getId())
                .name(spot.getName())
                .description(spot.getDescription())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .imagePath(spot.getImagePath())
                .imageUrls(spot.getImageUrls())
                .creator(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                .build())
            .collect(Collectors.toList());

        System.out.println("✅ Résultats: " + totalResults + " spots trouvés\n");

        return SearchResponseDto.<SpotDto>builder()
            .message("✅ Recherche terminée")
            .results(spotDtos)
            .totalResults(totalResults)
            .page(page)
            .totalPages(totalPages)
            .metadata(SearchMetadata.builder()
                .query(request.getQuery())
                .centerLatitude(request.getLatitude())
                .centerLongitude(request.getLongitude())
                .radiusKm(request.getRadius() != null ? request.getRadius() / 1000 : null)
                .creatorEmail(request.getCreatorEmail())
                .build())
            .build();
    }

    /**
     * 🔍 Recherche de HikingSpots
     */
    public SearchResponseDto<HikingSpotDto> searchHikingSpots(SearchRequestDto request) {
        /*System.out.println("🔍 === SEARCH HIKING SPOTS ===");
        System.out.println("Query: " + request.getQuery());*/
        
        List<HikingSpot> allSpots = hikingSpotRepository.findAll();
        List<HikingSpot> filteredSpots = new ArrayList<>(allSpots);

        // 1️⃣ Filtre textuel
        if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            String query = request.getQuery().toLowerCase();
            filteredSpots = filteredSpots.stream()
                .filter(spot -> 
                    (spot.getName() != null && spot.getName().toLowerCase().contains(query)) ||
                    (spot.getDescription() != null && spot.getDescription().toLowerCase().contains(query)) ||
                    (spot.getRegion() != null && spot.getRegion().toLowerCase().contains(query))
                )
                .collect(Collectors.toList());
            System.out.println("  Après filtre textuel: " + filteredSpots.size());
        }

        // 2️⃣ Filtre par créateur
        if (request.getCreatorEmail() != null && !request.getCreatorEmail().trim().isEmpty()) {
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getCreator() != null && 
                    spot.getCreator().getEmail().equalsIgnoreCase(request.getCreatorEmail()))
                .collect(Collectors.toList());
            System.out.println("  Après filtre créateur: " + filteredSpots.size());
        }

        // 3️⃣ Filtre par région
        if (request.getRegion() != null && !request.getRegion().trim().isEmpty()) {
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getRegion() != null && 
                    spot.getRegion().equalsIgnoreCase(request.getRegion()))
                .collect(Collectors.toList());
            System.out.println("  Après filtre région: " + filteredSpots.size());
        }

     // 4️⃣ Filtre par difficulté avec mapping texte → int
        if (request.getMinDifficulty() != null) {
            final int minDiff = request.getMinDifficulty();
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getDifficultyLevel() >= minDiff)
                .collect(Collectors.toList());
            System.out.println("  Après filtre difficulté min: " + filteredSpots.size());
        }

        if (request.getMaxDifficulty() != null) {
            final int maxDiff = request.getMaxDifficulty();
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getDifficultyLevel() <= maxDiff)
                .collect(Collectors.toList());
            System.out.println("  Après filtre difficulté max: " + filteredSpots.size());
        }

        // 5️⃣ Filtre par distance du parcours
        if (request.getMinDistance() != null) {
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getDistance() >= request.getMinDistance())
                .collect(Collectors.toList());
        }
        if (request.getMaxDistance() != null) {
            filteredSpots = filteredSpots.stream()
                .filter(spot -> spot.getDistance() <= request.getMaxDistance())
                .collect(Collectors.toList());
        }

        // 6️⃣ Filtre géographique (point de départ)
        if (request.getLatitude() != null && request.getLongitude() != null && request.getRadius() != null) {
            final double lat = request.getLatitude();
            final double lon = request.getLongitude();
            final double radiusMeters = request.getRadius();
            
            filteredSpots = filteredSpots.stream()
                .filter(spot -> {
                    double distance = calculateDistance(lat, lon, spot.getStartLatitude(), spot.getStartLongitude());
                    return distance <= radiusMeters;
                })
                .collect(Collectors.toList());
            System.out.println("  Après filtre géographique: " + filteredSpots.size());
        }

        // 7️⃣ Tri
        filteredSpots = sortHikingSpots(filteredSpots, request);

        // 8️⃣ Pagination
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        int totalResults = filteredSpots.size();
        int totalPages = (int) Math.ceil((double) totalResults / size);

        int start = page * size;
        int end = Math.min(start + size, totalResults);
        
        List<HikingSpot> paginatedSpots = start < totalResults 
            ? filteredSpots.subList(start, end) 
            : new ArrayList<>();

        // 9️⃣ Conversion en DTO
        List<HikingSpotDto> hikingSpotDtos = paginatedSpots.stream()
            .map(spot -> HikingSpotDto.builder()
                .id(spot.getId())
                .name(spot.getName())
                .description(spot.getDescription())
                .region(spot.getRegion())
                .distance(spot.getDistance())
                .imagePath(spot.getImagePath())
                .imageUrls(spot.getImageUrls())
                .startLatitude(spot.getStartLatitude())
                .startLongitude(spot.getStartLongitude())
                .endLatitude(spot.getEndLatitude())
                .endLongitude(spot.getEndLongitude())
                .difficultyLevel(spot.getDifficultyLevel())
                .creatorName(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                .build())
            .collect(Collectors.toList());

        //System.out.println("✅ Résultats: " + totalResults + " hiking spots trouvés\n");

        return SearchResponseDto.<HikingSpotDto>builder()
            .message("✅ Recherche terminée")
            .results(hikingSpotDtos)
            .totalResults(totalResults)
            .page(page)
            .totalPages(totalPages)
            .metadata(SearchMetadata.builder()
                .query(request.getQuery())
                .centerLatitude(request.getLatitude())
                .centerLongitude(request.getLongitude())
                .radiusKm(request.getRadius() != null ? request.getRadius() / 1000 : null)
                .region(request.getRegion())
                .creatorEmail(request.getCreatorEmail())
                .build())
            .build();
    }

    /**
     * 📏 Calcul de distance entre deux points (formule Haversine)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // Rayon de la Terre en mètres

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance en mètres
    }

    /**
     * 🔀 Tri des Spots
     */
    private List<Spot> sortSpots(List<Spot> spots, SearchRequestDto request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "name";
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "asc";
        boolean ascending = sortOrder.equalsIgnoreCase("asc");

        Comparator<Spot> comparator;

        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(Spot::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "distance":
                if (request.getLatitude() != null && request.getLongitude() != null) {
                    final double lat = request.getLatitude();
                    final double lon = request.getLongitude();
                    comparator = Comparator.comparingDouble(spot -> 
                        calculateDistance(lat, lon, spot.getLatitude(), spot.getLongitude()));
                } else {
                    comparator = Comparator.comparing(Spot::getName);
                }
                break;
            default:
                comparator = Comparator.comparing(Spot::getName);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return spots.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 🔀 Tri des HikingSpots
     */
    private List<HikingSpot> sortHikingSpots(List<HikingSpot> spots, SearchRequestDto request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "name";
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "asc";
        boolean ascending = sortOrder.equalsIgnoreCase("asc");

        Comparator<HikingSpot> comparator;

        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(HikingSpot::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "distance":
                if (request.getLatitude() != null && request.getLongitude() != null) {
                    final double lat = request.getLatitude();
                    final double lon = request.getLongitude();
                    comparator = Comparator.comparingDouble(spot -> 
                        calculateDistance(lat, lon, spot.getStartLatitude(), spot.getStartLongitude()));
                } else {
                    comparator = Comparator.comparingDouble(HikingSpot::getDistance);
                }
                break;
            case "parcours":
                comparator = Comparator.comparingDouble(HikingSpot::getDistance);
                break;
            default:
                comparator = Comparator.comparing(HikingSpot::getName);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return spots.stream().sorted(comparator).collect(Collectors.toList());
    }
}