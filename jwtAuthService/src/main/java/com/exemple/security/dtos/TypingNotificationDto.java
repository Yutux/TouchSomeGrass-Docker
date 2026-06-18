package com.exemple.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingNotificationDto {
    private Integer userId;
    private String username;
    private Integer conversationId;
    private boolean isTyping;
}