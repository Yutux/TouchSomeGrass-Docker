package com.exemple.security.entities;

import java.time.LocalDateTime;

import com.exemple.security.enums.GroupRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "group_memberships")
public class GroupMembership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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
    private UserApp user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({
        "hibernateLazyInitializer", 
        "handler", 
        "memberships",          
        "conversations",
        "creator"
    })
    private UserGroup group;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupRole role = GroupRole.MEMBER;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}