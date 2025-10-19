package com.exemple.security.entities;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "spots")
public class Spot implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String imagePath;
    
    private double latitude;
    private double longitude;
    private String description;
    
 // ✅ Plusieurs URLs d'images (Google Maps ou upload)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "spot_image_urls",
        joinColumns = @JoinColumn(name = "spot_id")
    )
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonBackReference
    private UserApp creator;
    }
