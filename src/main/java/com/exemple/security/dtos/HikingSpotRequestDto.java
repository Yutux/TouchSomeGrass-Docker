package com.exemple.security.dtos;


import java.util.List;

import com.exemple.security.entities.UserApp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HikingSpotRequestDto {

    private int id;
    private String name;
    private String description;
    private String region;

    // 🧭 Coordonnées de départ / arrivée
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;

    // 🧮 Données du trajet
    private double distance;     // en km
    private double duration;     // en minutes
    private String travelMode;   // WALKING, DRIVING, etc.
    private int difficultyLevel; // 1-5

    // 🖼️ Image principale + images additionnelles
    private String imagePath;         // image principale
    private List<String> imageUrls;   // autres images (ex: Google Maps photos)

    // 🗺️ Liste des escales (waypoints)
    private List<HikingWaypointDto> waypoints;

    // 👤 Informations créateur (optionnelles)
    private int creatorId;
    private UserApp creator;
}
