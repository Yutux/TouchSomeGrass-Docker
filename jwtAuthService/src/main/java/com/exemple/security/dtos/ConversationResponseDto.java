package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.Conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDto {
    private String message;
    private Conversation conversation;
    private List<Conversation> conversations;
}