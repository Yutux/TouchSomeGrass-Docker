package com.exemple.security.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.CommentResponseDto;
import com.exemple.security.dtos.CreateCommentDto;
import com.exemple.security.dtos.ReplyToCommentDto;
import com.exemple.security.entities.Comment;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.CommentStatus;
import com.exemple.security.repositories.CommentRepository;
import com.exemple.security.repositories.HikingSpotRepository;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;
    private final HikingSpotRepository hikingSpotRepository;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<CommentResponseDto> createCommentFromAuth(String authHeader, CreateCommentDto request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return createComment(user.getId(), request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> replyToCommentFromAuth(String authHeader, ReplyToCommentDto request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return replyToComment(user.getId(), request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> getUserCommentsFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getUserComments(user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> deleteOwnCommentFromAuth(String authHeader, int commentId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return deleteOwnComment(commentId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> hideCommentFromAuth(String authHeader, int commentId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return hideComment(commentId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> showCommentFromAuth(String authHeader, int commentId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return showComment(commentId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> deleteCommentAsOwnerFromAuth(String authHeader, int commentId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return deleteCommentAsOwner(commentId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> getAllSpotCommentsAsOwnerFromAuth(String authHeader, int spotId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getAllSpotCommentsAsOwner(spotId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<CommentResponseDto> getAllHikingSpotCommentsAsOwnerFromAuth(String authHeader, int hikingSpotId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommentResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getAllHikingSpotCommentsAsOwner(hikingSpotId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommentResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    // ===== MÉTHODES PUBLIQUES (INCHANGÉES) =====

    @Override
    public ResponseEntity<CommentResponseDto> getSpotComments(int spotId) {
        spotRepository.findById(spotId)
            .orElseThrow(() -> new RuntimeException("Spot non trouvé"));
        
        List<Comment> comments = commentRepository.findVisibleCommentsBySpotId(spotId, CommentStatus.VISIBLE);
        Double averageRating = commentRepository.getAverageRatingBySpotId(spotId);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaires récupérés avec succès")
                .comments(comments)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalComments(comments.size())
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> getHikingSpotComments(int hikingSpotId) {
        hikingSpotRepository.findById(hikingSpotId)
            .orElseThrow(() -> new RuntimeException("Hiking spot non trouvé"));
        
        List<Comment> comments = commentRepository.findVisibleCommentsByHikingSpotId(hikingSpotId, CommentStatus.VISIBLE);
        Double averageRating = commentRepository.getAverageRatingByHikingSpotId(hikingSpotId);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaires récupérés avec succès")
                .comments(comments)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalComments(comments.size())
                .build());
    }

    @Override
    public Double getSpotAverageRating(int spotId) {
        Double avg = commentRepository.getAverageRatingBySpotId(spotId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public Double getHikingSpotAverageRating(int hikingSpotId) {
        Double avg = commentRepository.getAverageRatingByHikingSpotId(hikingSpotId);
        return avg != null ? avg : 0.0;
    }

    // ===== MÉTHODES INTERNES  =====

    @Override
    public ResponseEntity<CommentResponseDto> createComment(int userId, CreateCommentDto request) {
        
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        if (request.getRating() < 1 || request.getRating() > 5) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("La note doit être entre 1 et 5")
                    .build());
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Le contenu du commentaire ne peut pas être vide")
                    .build());
        }
        
        Comment comment = Comment.builder()
            .user(user)
            .content(request.getContent())
            .rating(request.getRating())
            .status(CommentStatus.VISIBLE)
            .build();
        
        if (request.getSpotId() != null) {
            Spot spot = spotRepository.findById(request.getSpotId())
                .orElseThrow(() -> new RuntimeException("Spot non trouvé"));
            
            if (commentRepository.hasUserCommentedOnSpot(userId, request.getSpotId())) {
                return ResponseEntity.badRequest()
                    .body(CommentResponseDto.builder()
                        .message("Vous avez déjà commenté ce spot")
                        .build());
            }
            
            comment.setSpot(spot);
        } else if (request.getHikingSpotId() != null) {
            HikingSpot hikingSpot = hikingSpotRepository.findById(request.getHikingSpotId())
                .orElseThrow(() -> new RuntimeException("Hiking spot non trouvé"));
            
            if (commentRepository.hasUserCommentedOnHikingSpot(userId, request.getHikingSpotId())) {
                return ResponseEntity.badRequest()
                    .body(CommentResponseDto.builder()
                        .message("Vous avez déjà commenté ce hiking spot")
                        .build());
            }
            
            comment.setHikingSpot(hikingSpot);
        } else {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous devez spécifier un spot ou un hiking spot")
                    .build());
        }
        
        Comment savedComment = commentRepository.save(comment);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaire créé avec succès")
                .comment(savedComment)
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> replyToComment(int userId, ReplyToCommentDto request) {
       
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        Comment parentComment = commentRepository.findById(request.getParentCommentId())
            .orElseThrow(() -> new RuntimeException("Commentaire parent non trouvé"));
        
        if (request.getRating() < 1 || request.getRating() > 5) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("La note doit être entre 1 et 5")
                    .build());
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Le contenu de la réponse ne peut pas être vide")
                    .build());
        }
        
        Comment reply = Comment.builder()
            .user(user)
            .content(request.getContent())
            .rating(request.getRating())
            .parentComment(parentComment)
            .status(CommentStatus.VISIBLE)
            .build();
        
        if (parentComment.getSpot() != null) {
            reply.setSpot(parentComment.getSpot());
        } else if (parentComment.getHikingSpot() != null) {
            reply.setHikingSpot(parentComment.getHikingSpot());
        }
        
        Comment savedReply = commentRepository.save(reply);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Réponse au commentaire créée avec succès")
                .comment(savedReply)
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> getUserComments(int userId) {
        
        userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        List<Comment> comments = commentRepository.findByUserId(userId);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaires de l'utilisateur récupérés avec succès")
                .comments(comments)
                .totalComments(comments.size())
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> deleteOwnComment(int commentId, int userId) {
        // ... votre code existant inchangé ...
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        if (comment.getUser().getId() != userId) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous n'êtes pas autorisé à supprimer ce commentaire")
                    .build());
        }
        
        commentRepository.delete(comment);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaire supprimé avec succès")
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> hideComment(int commentId, int ownerId) {
       
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        boolean isOwner = false;
        if (comment.getSpot() != null) {
            isOwner = comment.getSpot().getCreator().getId() == ownerId;
        } else if (comment.getHikingSpot() != null) {
            isOwner = comment.getHikingSpot().getCreator().getId() == ownerId;
        }
        
        if (!isOwner) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous n'êtes pas autorisé à cacher ce commentaire")
                    .build());
        }
        
        comment.setStatus(CommentStatus.HIDDEN_BY_OWNER);
        comment.setHiddenByOwner(true);
        commentRepository.save(comment);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaire caché avec succès")
                .comment(comment)
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> showComment(int commentId, int ownerId) {
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        boolean isOwner = false;
        if (comment.getSpot() != null) {
            isOwner = comment.getSpot().getCreator().getId() == ownerId;
        } else if (comment.getHikingSpot() != null) {
            isOwner = comment.getHikingSpot().getCreator().getId() == ownerId;
        }
        
        if (!isOwner) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous n'êtes pas autorisé à rendre visible ce commentaire")
                    .build());
        }
        
        comment.setStatus(CommentStatus.VISIBLE);
        comment.setHiddenByOwner(false);
        commentRepository.save(comment);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaire rendu visible avec succès")
                .comment(comment)
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> deleteCommentAsOwner(int commentId, int ownerId) {
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        boolean isOwner = false;
        if (comment.getSpot() != null) {
            isOwner = comment.getSpot().getCreator().getId() == ownerId;
        } else if (comment.getHikingSpot() != null) {
            isOwner = comment.getHikingSpot().getCreator().getId() == ownerId;
        }
        
        if (!isOwner) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous n'êtes pas autorisé à supprimer ce commentaire")
                    .build());
        }
        
        commentRepository.delete(comment);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Commentaire supprimé avec succès par le propriétaire")
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> getAllSpotCommentsAsOwner(int spotId, int ownerId) {
        
        Spot spot = spotRepository.findById(spotId)
            .orElseThrow(() -> new RuntimeException("Spot non trouvé"));
        
        if (spot.getCreator().getId() != ownerId) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous n'êtes pas le propriétaire de ce spot")
                    .build());
        }
        
        List<Comment> comments = commentRepository.findBySpotId(spotId);
        Double averageRating = commentRepository.getAverageRatingBySpotId(spotId);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Tous les commentaires récupérés (incluant cachés)")
                .comments(comments)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalComments(comments.size())
                .build());
    }

    @Override
    public ResponseEntity<CommentResponseDto> getAllHikingSpotCommentsAsOwner(int hikingSpotId, int ownerId) {
        
        HikingSpot hikingSpot = hikingSpotRepository.findById(hikingSpotId)
            .orElseThrow(() -> new RuntimeException("Hiking spot non trouvé"));
        
        if (hikingSpot.getCreator().getId() != ownerId) {
            return ResponseEntity.badRequest()
                .body(CommentResponseDto.builder()
                    .message("Vous n'êtes pas le propriétaire de ce hiking spot")
                    .build());
        }
        
        List<Comment> comments = commentRepository.findByHikingSpotId(hikingSpotId);
        Double averageRating = commentRepository.getAverageRatingByHikingSpotId(hikingSpotId);
        
        return ResponseEntity.ok()
            .body(CommentResponseDto.builder()
                .message("Tous les commentaires récupérés (incluant cachés)")
                .comments(comments)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalComments(comments.size())
                .build());
    }
}