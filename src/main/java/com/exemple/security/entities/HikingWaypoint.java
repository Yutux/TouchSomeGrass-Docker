package com.exemple.security.entities;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hiking_waypoints")
public class HikingWaypoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;       // Nom du lieu
    private String address;    // Adresse complète
    private String placeId;    // Google Place ID
    private double latitude;
    private double longitude;
    private Double rating;     // Note Google Maps (optionnelle)

    // ✅ Liste des photos (URLs Google Maps)
    @ElementCollection
    @CollectionTable(
        name = "hiking_waypoint_photos",
        joinColumns = @JoinColumn(name = "waypoint_id")
    )
    @Column(name = "photo_url")
    private List<String> photos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiking_spot_id")
    @JsonIgnore
    private HikingSpot hikingSpot;
}
