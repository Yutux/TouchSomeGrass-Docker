package com.exemple.security.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hiking_spots")
public class HikingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private String region;
    private double distance;
    private String imagePath;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private int difficultyLevel;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonBackReference
    private UserApp creator;
}