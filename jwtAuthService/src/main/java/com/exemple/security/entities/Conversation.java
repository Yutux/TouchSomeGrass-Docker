package com.exemple.security.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exemple.security.enums.ConversationType;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
@Table(name = "conversations")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConversationType type = ConversationType.PRIVATE;
    
    // Pour les conversations de groupe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "conversations"})
    private UserGroup group;
    
    // Créateur de la conversation
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
    
    // Participants (pour les conversations privées)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
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
    private List<UserApp> participants = new ArrayList<>();
    
    // Messages de la conversation
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "conversation"})
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
    }
}