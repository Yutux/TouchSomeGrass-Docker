package com.exemple.security.entities;

import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
//import jakarta.persistence.OneToMany;
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
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "friends", "favoriteSpots", "favoriteHikingSpots", "sentMessages", "receivedMessages"})
    private UserApp sender;
    
    // Pour les messages dans une conversation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "messages"})
    private Conversation conversation;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}