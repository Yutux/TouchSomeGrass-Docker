package com.exemple.security.services;

import com.exemple.security.dtos.FriendRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface FriendRequestService {
    
    // ===== MÉTHODES AVEC AUTHENTIFICATION =====
    
    /**
     * Envoyer une demande d'ami
     */
    ResponseEntity<Map<String, Object>> sendFriendRequestFromAuth(String authHeader, Integer receiverId);
    
    /**
     * Accepter une demande d'ami
     */
    ResponseEntity<Map<String, Object>> acceptFriendRequestFromAuth(String authHeader, Integer requestId);
    
    /**
     * Refuser une demande d'ami
     */
    ResponseEntity<Map<String, Object>> declineFriendRequestFromAuth(String authHeader, Integer requestId);
    
    /**
     * Annuler une demande d'ami envoyée
     */
    ResponseEntity<Map<String, Object>> cancelFriendRequestFromAuth(String authHeader, Integer requestId);
    
    /**
     * Obtenir les demandes reçues PENDING
     */
    ResponseEntity<Map<String, Object>> getPendingReceivedRequestsFromAuth(String authHeader);
    
    /**
     * Compter les demandes reçues PENDING
     */
    ResponseEntity<Map<String, Object>> countPendingReceivedRequestsFromAuth(String authHeader);
    
    /**
     * Obtenir les demandes envoyées
     */
    ResponseEntity<Map<String, Object>> getSentRequestsFromAuth(String authHeader);
    
    // ===== MÉTHODES INTERNES =====
    
    /**
     * Créer une demande d'ami
     */
    FriendRequestDTO createFriendRequest(Integer senderId, Integer receiverId);
    
    /**
     * Accepter une demande
     */
    void acceptRequest(Integer requestId, Integer userId);
    
    /**
     * Refuser une demande
     */
    void declineRequest(Integer requestId, Integer userId);
    
    /**
     * Annuler une demande
     */
    void cancelRequest(Integer requestId, Integer userId);
    
    /**
     * Obtenir les demandes reçues PENDING pour un utilisateur
     */
    List<FriendRequestDTO> getPendingReceivedRequests(Integer userId);
    
    /**
     * Compter les demandes reçues PENDING
     */
    long countPendingReceivedRequests(Integer userId);
    
    /**
     * Obtenir les demandes envoyées par un utilisateur
     */
    List<FriendRequestDTO> getSentRequests(Integer userId);
}