package com.exemple.security.services;

import org.springframework.http.ResponseEntity;

import com.exemple.security.dtos.MessageResponseDto;

public interface MessageService {

    // ===== MÉTHODES AVEC AUTHENTIFICATION (authHeader) =====
    
    ResponseEntity<MessageResponseDto> getSentMessagesFromAuth(String authHeader);
    ResponseEntity<MessageResponseDto> getUnreadMessagesFromAuth(String authHeader);
    ResponseEntity<Long> countUnreadMessagesFromAuth(String authHeader);
    
    // ===== MÉTHODES INTERNES =====
    
    ResponseEntity<MessageResponseDto> getSentMessages(int userId);
    ResponseEntity<MessageResponseDto> getUnreadMessages(int userId);
    long countUnreadMessages(int userId);
}