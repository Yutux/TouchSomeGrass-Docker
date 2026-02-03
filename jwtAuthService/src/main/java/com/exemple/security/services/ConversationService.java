package com.exemple.security.services;

import org.springframework.http.ResponseEntity;

import com.exemple.security.dtos.ConversationResponseDto;
import com.exemple.security.dtos.CreateConversationDto;
import com.exemple.security.dtos.MessageResponseDto;
import com.exemple.security.dtos.SendConversationMessageDto;

public interface ConversationService {

    // ===== MÉTHODES AVEC AUTHENTIFICATION (authHeader) =====
    
    ResponseEntity<ConversationResponseDto> createPrivateConversationFromAuth(String authHeader, CreateConversationDto request);
    ResponseEntity<ConversationResponseDto> createGroupConversationFromAuth(String authHeader, CreateConversationDto request);
    ResponseEntity<ConversationResponseDto> getUserConversationsFromAuth(String authHeader);
    ResponseEntity<ConversationResponseDto> getGroupConversationsFromAuth(String authHeader, int groupId);
    ResponseEntity<ConversationResponseDto> getConversationByIdFromAuth(String authHeader, int conversationId);
    ResponseEntity<MessageResponseDto> sendMessageInConversationFromAuth(String authHeader, SendConversationMessageDto request);
    ResponseEntity<MessageResponseDto> getConversationMessagesFromAuth(String authHeader, int conversationId);
    ResponseEntity<MessageResponseDto> markConversationMessagesAsReadFromAuth(String authHeader, int conversationId);
    ResponseEntity<ConversationResponseDto> deleteConversationFromAuth(String authHeader, int conversationId);
    
    // ===== MÉTHODES INTERNES =====
    
    ResponseEntity<ConversationResponseDto> createPrivateConversation(int creatorId, CreateConversationDto request);
    ResponseEntity<ConversationResponseDto> createGroupConversation(int creatorId, CreateConversationDto request);
    ResponseEntity<ConversationResponseDto> getUserConversations(int userId);
    ResponseEntity<ConversationResponseDto> getGroupConversations(int groupId, int userId);
    ResponseEntity<ConversationResponseDto> getConversationById(int conversationId, int userId);
    ResponseEntity<MessageResponseDto> sendMessageInConversation(int senderId, SendConversationMessageDto request);
    ResponseEntity<MessageResponseDto> getConversationMessages(int conversationId, int userId);
    ResponseEntity<MessageResponseDto> markConversationMessagesAsRead(int conversationId, int userId);
    ResponseEntity<ConversationResponseDto> deleteConversation(int conversationId, int userId);
}