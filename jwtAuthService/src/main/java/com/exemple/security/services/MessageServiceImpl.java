package com.exemple.security.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.MessageResponseDto;
import com.exemple.security.entities.Message;
import com.exemple.security.entities.UserApp;
import com.exemple.security.repositories.MessageRepository;
import com.exemple.security.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<MessageResponseDto> getSentMessagesFromAuth(String authHeader) {
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

            return getSentMessages(user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<MessageResponseDto> getUnreadMessagesFromAuth(String authHeader) {
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

            return getUnreadMessages(user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<Long> countUnreadMessagesFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return ResponseEntity.ok(countUnreadMessages(user.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== MÉTHODES INTERNES =====

    @Override
    public ResponseEntity<MessageResponseDto> getSentMessages(int userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        List<Message> messages = messageRepository.findBySenderId(userId);
        
        return ResponseEntity.ok()
            .body(MessageResponseDto.builder()
                .message("Messages envoyés récupérés avec succès")
                .messages(messages)
                .build());
    }

    @Override
    public ResponseEntity<MessageResponseDto> getUnreadMessages(int userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        List<Message> messages = messageRepository.findUnreadMessagesByUserId(userId);
        long unreadCount = messageRepository.countUnreadMessagesByUserId(userId);
        
        return ResponseEntity.ok()
            .body(MessageResponseDto.builder()
                .message("Messages non lus récupérés avec succès")
                .messages(messages)
                .unreadCount(unreadCount)
                .build());
    }

    @Override
    public long countUnreadMessages(int userId) {
        return messageRepository.countUnreadMessagesByUserId(userId);
    }
}