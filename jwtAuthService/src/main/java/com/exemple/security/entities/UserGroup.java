package com.exemple.security.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_groups")
public class UserGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String imageUrl;
    
    // ✅ Créateur du groupe - NE PAS sérialiser pour éviter les boucles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({
        "hibernateLazyInitializer", 
        "handler", 
        "password",
        "roles",
        "spots",
        "hikingSpots",
        "favoriteSpots",
        "favoriteHikingSpots",
        "friends",
        "createdGroups",        
        "groupMemberships",
        "comments",
        "sentMessages",
        "conversations"
    })
    private UserApp creator;
    
    // ✅ Pour exposer les infos du créateur sans la relation complète
    @Column(name = "creator_id", insertable = false, updatable = false)
    private Integer creatorId;
    
    // Membres du groupe avec leurs rôles
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({
        "hibernateLazyInitializer", 
        "handler", 
        "password",
        "roles",
        "spots",
        "hikingSpots",
        "favoriteSpots",
        "favoriteHikingSpots",
        "friends",
        "createdGroups",      
        "groupMemberships",
        "comments",
        "sentMessages",
        "conversations"
    })
    @Builder.Default
    private List<GroupMembership> memberships = new ArrayList<>();
    
    // Conversations du groupe
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JsonIgnoreProperties({
        "hibernateLazyInitializer", 
        "handler", 
        "password",
        "roles",
        "spots",
        "hikingSpots",
        "favoriteSpots",
        "favoriteHikingSpots",
        "friends",
        "createdGroups",      
        "groupMemberships",
        "comments",
        "sentMessages",
        "conversations"
    })
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_private")
    @Builder.Default
    private boolean isPrivate = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}