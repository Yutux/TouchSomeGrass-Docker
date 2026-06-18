package com.exemple.security.web;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.exemple.security.dtos.ChatMessageDto;
import com.exemple.security.dtos.SendConversationMessageDto;
import com.exemple.security.dtos.TypingNotificationDto;
import com.exemple.security.dtos.UserStatusDto;
import com.exemple.security.entities.Message;
import com.exemple.security.entities.UserApp;
import com.exemple.security.repositories.MessageRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.services.ConversationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Envoyer un message dans une conversation
     * Client envoie vers : /app/chat.send
     * Broadcast vers : /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto messageDto, Authentication authentication) {
        try {
            log.info("Réception message WebSocket: {}", messageDto);
            
            // Récupérer l'utilisateur authentifié
            String email = authentication.getName();
            UserApp sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Sauvegarder le message via le service
            SendConversationMessageDto dto = new SendConversationMessageDto();
            dto.setConversationId(messageDto.getConversationId());
            dto.setContent(messageDto.getContent());
            
            var response = conversationService.sendMessageInConversation(sender.getId(), dto);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Message savedMessage = response.getBody().getMessageData();
                
                // Créer le DTO pour WebSocket
                ChatMessageDto broadcastDto = ChatMessageDto.builder()
                    .id(savedMessage.getId())
                    .content(savedMessage.getContent())
                    .senderId(sender.getId())
                    .senderName(sender.getFirstname() + " " + sender.getLastname())
                    .conversationId(messageDto.getConversationId())
                    .createdAt(savedMessage.getSentAt())
                    .isRead(false)
                    .type(ChatMessageDto.MessageType.TEXT)
                    .build();
                
                // Broadcast le message à tous les participants de la conversation
                messagingTemplate.convertAndSend(
                    "/topic/conversation/" + messageDto.getConversationId(),
                    broadcastDto
                );
                
                log.info("Message broadcasté avec succès vers /topic/conversation/{}", messageDto.getConversationId());
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message WebSocket", e);
        }
    }

    /**
     * Notification "utilisateur en train d'écrire"
     * Client envoie vers : /app/chat.typing
     * Broadcast vers : /topic/conversation/{conversationId}/typing
     */
    @MessageMapping("/chat.typing")
    public void typingNotification(@Payload TypingNotificationDto notification) {
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + notification.getConversationId() + "/typing",
            notification
        );
    }

    /**
     * Mise à jour du statut utilisateur (online/offline)
     * Client envoie vers : /app/user.status
     * Broadcast vers : /topic/user.status
     */
    @MessageMapping("/user.status")
    public void updateUserStatus(@Payload UserStatusDto status) {
        messagingTemplate.convertAndSend("/topic/user.status", status);
    }
}