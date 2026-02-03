package com.exemple.security.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.exemple.security.dtos.AuthenticationResponseDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;

public interface UserRelationService {

    // ===== MÉTHODES AVEC AUTHENTIFICATION (authHeader) =====
    
    // Favoris Spots
    ResponseEntity<AuthenticationResponseDto> addFavoriteSpotFromAuth(String authHeader, int spotId);
    ResponseEntity<AuthenticationResponseDto> removeFavoriteSpotFromAuth(String authHeader, int spotId);
    ResponseEntity<List<Spot>> getFavoriteSpotsFromAuth(String authHeader);
    
    // Favoris Hiking Spots
    ResponseEntity<AuthenticationResponseDto> addFavoriteHikingSpotFromAuth(String authHeader, int hikingSpotId);
    ResponseEntity<AuthenticationResponseDto> removeFavoriteHikingSpotFromAuth(String authHeader, int hikingSpotId);
    ResponseEntity<List<HikingSpot>> getFavoriteHikingSpotsFromAuth(String authHeader);
    
    // Amis
    ResponseEntity<AuthenticationResponseDto> addFriendFromAuth(String authHeader, int friendId);
    ResponseEntity<AuthenticationResponseDto> removeFriendFromAuth(String authHeader, int friendId);
    ResponseEntity<List<UserApp>> getFriendsFromAuth(String authHeader);
    
    // ===== MÉTHODES INTERNES (utilisées par les méthodes FromAuth) =====
    
    ResponseEntity<AuthenticationResponseDto> addFavoriteSpot(int userId, int spotId);
    ResponseEntity<AuthenticationResponseDto> removeFavoriteSpot(int userId, int spotId);
    List<Spot> getFavoriteSpots(int userId);
    
    ResponseEntity<AuthenticationResponseDto> addFavoriteHikingSpot(int userId, int hikingSpotId);
    ResponseEntity<AuthenticationResponseDto> removeFavoriteHikingSpot(int userId, int hikingSpotId);
    List<HikingSpot> getFavoriteHikingSpots(int userId);
    
    ResponseEntity<AuthenticationResponseDto> addFriend(int userId, int friendId);
    ResponseEntity<AuthenticationResponseDto> removeFriend(int userId, int friendId);
    List<UserApp> getFriends(int userId);
}