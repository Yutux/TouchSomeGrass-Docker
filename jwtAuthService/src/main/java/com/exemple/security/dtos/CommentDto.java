package com.exemple.security.dtos;

import java.time.LocalDateTime;

import com.exemple.security.enums.CommentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private int id;
    private int userId;
    private String userName;
    private String content;
    private int rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CommentStatus status;
    private boolean hiddenByOwner;
    private Integer spotId;
    private String spotName;
    private Integer hikingSpotId;
    private String hikingSpotName;
}