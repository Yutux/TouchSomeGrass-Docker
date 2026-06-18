package com.exemple.security.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exemple.security.enums.CommentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "friends", "favoriteSpots", "favoriteHikingSpots", "sentMessages", "receivedMessages", "comments"})
    private UserApp user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "creator", "comments"})
    private Spot spot;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiking_spot_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "creator", "comments"})
    private HikingSpot hikingSpot;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private int rating; // Note de 1 à 5
    
    // Réponse à un autre commentaire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "replies", "spot", "hikingSpot"})
    private Comment parentComment;
    
    // Liste des réponses à ce commentaire
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentComment"})
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CommentStatus status = CommentStatus.VISIBLE;
    
    @Column(name = "hidden_by_owner")
    @Builder.Default
    private boolean hiddenByOwner = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}