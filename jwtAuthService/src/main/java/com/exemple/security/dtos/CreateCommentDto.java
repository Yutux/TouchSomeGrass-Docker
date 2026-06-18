package com.exemple.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {
    private String content;
    private int rating; // 1 à 5
    private Integer spotId;
    private Integer hikingSpotId;
}