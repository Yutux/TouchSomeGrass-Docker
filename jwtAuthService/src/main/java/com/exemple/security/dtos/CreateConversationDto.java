package com.exemple.security.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationDto {
    private String title;
    private Integer groupId; // Pour une conversation de groupe
    private List<Integer> participantIds; // Pour une conversation privée
}