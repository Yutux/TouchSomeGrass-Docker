package com.exemple.security.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "creator"})
@Table(name = "hiking_spots")
public class HikingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name; // Nom du parcours (ex: "Circuit de la forêt")
    private String description;
    private String region;
    private double distance; // Distance totale (en km)
    private double duration; // Durée totale (en minutes)
    private String travelMode; // WALKING, DRIVING, etc.

    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;

    private int difficultyLevel; // 1-5
    private String imagePath; // Image principale

    @ElementCollection
    @CollectionTable(
        name = "hiking_spot_images",
        joinColumns = @JoinColumn(name = "hiking_spot_id")
    )
    @Column(name = "image_url")
    private List<String> imageUrls;

    // ✅ Liste des escales (waypoints)
    @OneToMany(mappedBy = "hikingSpot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HikingWaypoint> waypoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonBackReference
    private UserApp creator;

    @OneToMany(mappedBy = "hikingSpot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Comment> comments = new ArrayList<>();
}