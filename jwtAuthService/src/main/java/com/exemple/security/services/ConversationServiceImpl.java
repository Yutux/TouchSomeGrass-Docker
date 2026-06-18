package com.exemple.security.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.ConversationResponseDto;
import com.exemple.security.dtos.CreateConversationDto;
import com.exemple.security.dtos.MessageResponseDto;
import com.exemple.security.dtos.SendConversationMessageDto;
import com.exemple.security.entities.Conversation;
import com.exemple.security.entities.Message;
import com.exemple.security.entities.UserApp;
import com.exemple.security.entities.UserGroup;
import com.exemple.security.enums.ConversationType;
import com.exemple.security.repositories.ConversationRepository;
import com.exemple.security.repositories.GroupMembershipRepository;
import com.exemple.security.repositories.MessageRepository;
import com.exemple.security.repositories.UserGroupRepository;
import com.exemple.security.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupService groupService;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<ConversationResponseDto> createPrivateConversationFromAuth(String authHeader, CreateConversationDto request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return createPrivateConversation(creator.getId(), request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<ConversationResponseDto> createGroupConversationFromAuth(String authHeader, CreateConversationDto request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return createGroupConversation(creator.getId(), request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<ConversationResponseDto> getUserConversationsFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getUserConversations(user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<ConversationResponseDto> getGroupConversationsFromAuth(String authHeader, int groupId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getGroupConversations(groupId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<ConversationResponseDto> getConversationByIdFromAuth(String authHeader, int conversationId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getConversationById(conversationId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<MessageResponseDto> sendMessageInConversationFromAuth(String authHeader, SendConversationMessageDto request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return sendMessageInConversation(sender.getId(), request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<MessageResponseDto> getConversationMessagesFromAuth(String authHeader, int conversationId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getConversationMessages(conversationId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<MessageResponseDto> markConversationMessagesAsReadFromAuth(String authHeader, int conversationId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return markConversationMessagesAsRead(conversationId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<ConversationResponseDto> deleteConversationFromAuth(String authHeader, int conversationId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ConversationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return deleteConversation(conversationId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    // ===== MÉTHODES INTERNES =====

    @Override
    public ResponseEntity<ConversationResponseDto> createPrivateConversation(int creatorId, CreateConversationDto request) {
        
        UserApp creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new UsernameNotFoundException("Créateur non trouvé"));
        
        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Vous devez spécifier au moins un participant")
                    .build());
        }
        
        if (request.getParticipantIds().size() == 1) {
            int otherUserId = request.getParticipantIds().get(0);
            Optional<Conversation> existingConv = conversationRepository
                .findPrivateConversationBetweenUsers(creatorId, otherUserId);
            
            if (existingConv.isPresent()) {
                return ResponseEntity.ok()
                    .body(ConversationResponseDto.builder()
                        .message("Conversation existante")
                        .conversation(existingConv.get())
                        .build());
            }
        }
        
        Conversation conversation = Conversation.builder()
            .title(request.getTitle() != null ? request.getTitle() : "Conversation")
            .type(ConversationType.PRIVATE)
            .creator(creator)
            .build();
        
        conversation.getParticipants().add(creator);
        for (Integer participantId : request.getParticipantIds()) {
            UserApp participant = userRepository.findById(participantId)
                .orElseThrow(() -> new UsernameNotFoundException("Participant non trouvé: " + participantId));
            conversation.getParticipants().add(participant);
        }
        
        Conversation savedConversation = conversationRepository.save(conversation);
        
        return ResponseEntity.ok()
            .body(ConversationResponseDto.builder()
                .message("Conversation créée avec succès")
                .conversation(savedConversation)
                .build());
    }

    @Override
    public ResponseEntity<ConversationResponseDto> createGroupConversation(int creatorId, CreateConversationDto request) {
       
        UserApp creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new UsernameNotFoundException("Créateur non trouvé"));
        
        if (request.getGroupId() == null) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Vous devez spécifier un groupe")
                    .build());
        }
        
        UserGroup group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        if (!groupService.canUserCreateConversations(creatorId, request.getGroupId())) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Vous n'avez pas la permission de créer des conversations dans ce groupe")
                    .build());
        }
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Le titre de la conversation ne peut pas être vide")
                    .build());
        }
        
        Conversation conversation = Conversation.builder()
            .title(request.getTitle())
            .type(ConversationType.GROUP)
            .group(group)
            .creator(creator)
            .build();
        
        Conversation savedConversation = conversationRepository.save(conversation);
        
        return ResponseEntity.ok()
            .body(ConversationResponseDto.builder()
                .message("Conversation de groupe créée avec succès")
                .conversation(savedConversation)
                .build());
    }

    @Override
    public ResponseEntity<ConversationResponseDto> getUserConversations(int userId) {
      
        userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        List<Conversation> conversations = conversationRepository.findAllUserConversations(userId);
        
        return ResponseEntity.ok()
            .body(ConversationResponseDto.builder()
                .message("Conversations récupérées avec succès")
                .conversations(conversations)
                .build());
    }

    @Override
    public ResponseEntity<ConversationResponseDto> getGroupConversations(int groupId, int userId) {
        
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        if (!membershipRepository.isUserMemberOfGroup(userId, groupId)) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Vous n'êtes pas membre de ce groupe")
                    .build());
        }
        
        List<Conversation> conversations = conversationRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        
        return ResponseEntity.ok()
            .body(ConversationResponseDto.builder()
                .message("Conversations du groupe récupérées")
                .conversations(conversations)
                .build());
    }

    @Override
    public ResponseEntity<ConversationResponseDto> getConversationById(int conversationId, int userId) {
       
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        boolean hasAccess = false;
        if (conversation.getType() == ConversationType.PRIVATE) {
            hasAccess = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId() == userId);
        } else if (conversation.getType() == ConversationType.GROUP) {
            hasAccess = membershipRepository.isUserMemberOfGroup(userId, conversation.getGroup().getId());
        }
        
        if (!hasAccess) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Vous n'avez pas accès à cette conversation")
                    .build());
        }
        
        return ResponseEntity.ok()
            .body(ConversationResponseDto.builder()
                .message("Conversation trouvée")
                .conversation(conversation)
                .build());
    }

    @Override
    public ResponseEntity<MessageResponseDto> sendMessageInConversation(int senderId, SendConversationMessageDto request) {
        
        UserApp sender = userRepository.findById(senderId)
            .orElseThrow(() -> new UsernameNotFoundException("Expéditeur non trouvé"));
        
        Conversation conversation = conversationRepository.findById(request.getConversationId())
            .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        boolean hasAccess = false;
        if (conversation.getType() == ConversationType.PRIVATE) {
            hasAccess = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId() == senderId);
        } else if (conversation.getType() == ConversationType.GROUP) {
            hasAccess = membershipRepository.isUserMemberOfGroup(senderId, conversation.getGroup().getId());
        }
        
        if (!hasAccess) {
            return ResponseEntity.badRequest()
                .body(MessageResponseDto.builder()
                    .message("Vous n'avez pas accès à cette conversation")
                    .build());
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(MessageResponseDto.builder()
                    .message("Le contenu du message ne peut pas être vide")
                    .build());
        }
        
        Message message = Message.builder()
            .sender(sender)
            .conversation(conversation)
            .content(request.getContent())
            .isRead(false)
            .build();
        
        Message savedMessage = messageRepository.save(message);
        
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return ResponseEntity.ok()
            .body(MessageResponseDto.builder()
                .message("Message envoyé avec succès")
                .messageData(savedMessage)
                .build());
    }

    @Override
    public ResponseEntity<MessageResponseDto> getConversationMessages(int conversationId, int userId) {
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        boolean hasAccess = false;
        if (conversation.getType() == ConversationType.PRIVATE) {
            hasAccess = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId() == userId);
        } else if (conversation.getType() == ConversationType.GROUP) {
            hasAccess = membershipRepository.isUserMemberOfGroup(userId, conversation.getGroup().getId());
        }
        
        if (!hasAccess) {
            return ResponseEntity.badRequest()
                .body(MessageResponseDto.builder()
                    .message("Vous n'avez pas accès à cette conversation")
                    .build());
        }
        
        List<Message> messages = messageRepository.findByConversationId(conversationId);
        
        return ResponseEntity.ok()
            .body(MessageResponseDto.builder()
                .message("Messages récupérés avec succès")
                .messages(messages)
                .build());
    }

    @Override
    public ResponseEntity<MessageResponseDto> markConversationMessagesAsRead(int conversationId, int userId) {
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        List<Message> messages = messageRepository.findByConversationId(conversationId);
        
        messages.stream()
            .filter(m -> m.getSender().getId() != userId && !m.isRead())
            .forEach(m -> {
                m.setRead(true);
                m.setReadAt(LocalDateTime.now());
            });
        
        messageRepository.saveAll(messages);
        
        return ResponseEntity.ok()
            .body(MessageResponseDto.builder()
                .message("Messages marqués comme lus")
                .build());
    }

    @Override
    public ResponseEntity<ConversationResponseDto> deleteConversation(int conversationId, int userId) {
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        
        boolean canDelete = false;
        
        if (conversation.getType() == ConversationType.PRIVATE) {
            canDelete = conversation.getCreator().getId() == userId;
        } else if (conversation.getType() == ConversationType.GROUP) {
            canDelete = conversation.getCreator().getId() == userId || 
                       groupService.canUserCreateConversations(userId, conversation.getGroup().getId());
        }
        
        if (!canDelete) {
            return ResponseEntity.badRequest()
                .body(ConversationResponseDto.builder()
                    .message("Vous n'avez pas la permission de supprimer cette conversation")
                    .build());
        }
        
        conversationRepository.delete(conversation);
        
        return ResponseEntity.ok()
            .body(ConversationResponseDto.builder()
                .message("Conversation supprimée avec succès")
                .build());
    }
}