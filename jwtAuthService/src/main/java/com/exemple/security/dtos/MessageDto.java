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
public class MessageDto {
    private int id;
    private int senderId;
    private String senderName;
    private int receiverId;
    private String receiverName;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
    private LocalDateTime readAt;
}