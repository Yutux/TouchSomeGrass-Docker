package com.exemple.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {
    // Recherche textuelle
    private String query; // Recherche dans nom + description
    
    // Recherche géographique
    private Double latitude;
    private Double longitude;
    private Double radius; // Rayon en mètres (ex: 5000 = 5km)
    
    // Filtres généraux
    private String region;
    private Double minRating;
    private String creatorEmail; // 🔥 Filtrer par créateur
    
    // Filtres spécifiques HikingSpots
    private Integer minDifficulty; // 1-5
    private Integer maxDifficulty; // 1-5 // EASY, MODERATE, HARD
    private Double minDistance; // Distance minimum du parcours
    private Double maxDistance; // Distance maximum du parcours
    
    // Pagination
    private Integer page;
    private Integer size;
    
    // Tri
    private String sortBy; // name, distance, rating, createdAt
    private String sortOrder; // asc, desc
}