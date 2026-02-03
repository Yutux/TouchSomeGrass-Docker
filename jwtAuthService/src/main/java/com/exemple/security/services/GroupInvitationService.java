package com.exemple.security.services;

import com.exemple.security.dtos.GroupInvitationDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface GroupInvitationService {
    
    // ===== MÉTHODES AVEC AUTHENTIFICATION =====
    
    ResponseEntity<Map<String, Object>> sendInvitationFromAuth(String authHeader, int groupId, int userId);
    
    ResponseEntity<Map<String, Object>> acceptInvitationFromAuth(String authHeader, int invitationId);
    
    ResponseEntity<Map<String, Object>> declineInvitationFromAuth(String authHeader, int invitationId);
    
    ResponseEntity<Map<String, Object>> cancelInvitationFromAuth(String authHeader, int invitationId);
    
    ResponseEntity<Map<String, Object>> getPendingInvitationsFromAuth(String authHeader);
    
    ResponseEntity<Map<String, Object>> countPendingInvitationsFromAuth(String authHeader);
    
    ResponseEntity<Map<String, Object>> getGroupInvitationsFromAuth(String authHeader, int groupId);
    
    ResponseEntity<Map<String, Object>> getSentInvitationsFromAuth(String authHeader);
    
    // ===== MÉTHODES INTERNES =====
    
    GroupInvitationDTO createInvitation(int groupId, int userId, int invitedById);
    
    void acceptInvitation(int invitationId, int userId);
    
    void declineInvitation(int invitationId, int userId);
    
    void cancelInvitation(int invitationId, int userId);
    
    List<GroupInvitationDTO> getPendingInvitationsForUser(int userId);
    
    long countPendingInvitationsForUser(int userId);
    
    List<GroupInvitationDTO> getGroupInvitations(int groupId);
    
    List<GroupInvitationDTO> getSentInvitationsByUser(int userId);
}