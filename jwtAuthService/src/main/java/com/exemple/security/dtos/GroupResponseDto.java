package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.UserGroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDto {
    private String message;
    private UserGroup group;
    private List<UserGroup> groups;
}