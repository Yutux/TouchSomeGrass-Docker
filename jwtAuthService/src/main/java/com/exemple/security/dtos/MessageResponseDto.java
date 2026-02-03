package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private String message;
    private Message messageData;
    private List<Message> messages;
    private long unreadCount;
}