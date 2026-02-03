package com.exemple.security.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Integer id;
    private String content;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private Integer conversationId;
    private LocalDateTime createdAt;
    private boolean isRead;
    private MessageType type;
    
    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM, TYPING
    }
}