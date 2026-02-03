package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.Comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private String message;
    private Comment comment;
    private List<Comment> comments;
    private Double averageRating;
    private int totalComments;
}