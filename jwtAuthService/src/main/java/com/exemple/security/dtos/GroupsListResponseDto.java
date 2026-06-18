package com.exemple.security.dtos;

import com.exemple.security.entities.UserGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse de la liste de groupes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupsListResponseDto {
    private List<UserGroup> groups;
    private int count;
    
    // ✅ Constructeur utilitaire
    public static GroupsListResponseDto of(List<UserGroup> groups) {
        return GroupsListResponseDto.builder()
            .groups(groups)
            .count(groups.size())
            .build();
    }
}