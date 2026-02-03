package com.exemple.security.services;

import org.springframework.http.ResponseEntity;

import com.exemple.security.dtos.CommentResponseDto;
import com.exemple.security.dtos.CreateCommentDto;
import com.exemple.security.dtos.ReplyToCommentDto;

public interface CommentService {

    // ===== MÉTHODES AVEC AUTHENTIFICATION (authHeader) =====
    
    ResponseEntity<CommentResponseDto> createCommentFromAuth(String authHeader, CreateCommentDto request);
    ResponseEntity<CommentResponseDto> replyToCommentFromAuth(String authHeader, ReplyToCommentDto request);
    ResponseEntity<CommentResponseDto> getUserCommentsFromAuth(String authHeader);
    ResponseEntity<CommentResponseDto> deleteOwnCommentFromAuth(String authHeader, int commentId);
    ResponseEntity<CommentResponseDto> hideCommentFromAuth(String authHeader, int commentId);
    ResponseEntity<CommentResponseDto> showCommentFromAuth(String authHeader, int commentId);
    ResponseEntity<CommentResponseDto> deleteCommentAsOwnerFromAuth(String authHeader, int commentId);
    ResponseEntity<CommentResponseDto> getAllSpotCommentsAsOwnerFromAuth(String authHeader, int spotId);
    ResponseEntity<CommentResponseDto> getAllHikingSpotCommentsAsOwnerFromAuth(String authHeader, int hikingSpotId);
    
    // ===== MÉTHODES PUBLIQUES (sans authentification) =====
    
    ResponseEntity<CommentResponseDto> getSpotComments(int spotId);
    ResponseEntity<CommentResponseDto> getHikingSpotComments(int hikingSpotId);
    Double getSpotAverageRating(int spotId);
    Double getHikingSpotAverageRating(int hikingSpotId);
    
    // ===== MÉTHODES INTERNES =====
    
    ResponseEntity<CommentResponseDto> createComment(int userId, CreateCommentDto request);
    ResponseEntity<CommentResponseDto> replyToComment(int userId, ReplyToCommentDto request);
    ResponseEntity<CommentResponseDto> getUserComments(int userId);
    ResponseEntity<CommentResponseDto> deleteOwnComment(int commentId, int userId);
    ResponseEntity<CommentResponseDto> hideComment(int commentId, int ownerId);
    ResponseEntity<CommentResponseDto> showComment(int commentId, int ownerId);
    ResponseEntity<CommentResponseDto> deleteCommentAsOwner(int commentId, int ownerId);
    ResponseEntity<CommentResponseDto> getAllSpotCommentsAsOwner(int spotId, int ownerId);
    ResponseEntity<CommentResponseDto> getAllHikingSpotCommentsAsOwner(int hikingSpotId, int ownerId);
}