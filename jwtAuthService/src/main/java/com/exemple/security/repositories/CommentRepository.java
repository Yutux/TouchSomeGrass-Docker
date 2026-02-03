package com.exemple.security.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exemple.security.entities.Comment;
import com.exemple.security.enums.CommentStatus;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    
    // Commentaires d'un utilisateur
    List<Comment> findByUserId(int userId);
    
    // Commentaires visibles d'un spot
    @Query("SELECT c FROM Comment c WHERE c.spot.id = :spotId AND c.status = :status ORDER BY c.createdAt DESC")
    List<Comment> findVisibleCommentsBySpotId(@Param("spotId") int spotId, @Param("status") CommentStatus status);
    
    // Tous les commentaires d'un spot (pour le propriétaire)
    List<Comment> findBySpotId(int spotId);
    
    // Commentaires visibles d'un hiking spot 
    @Query("SELECT c FROM Comment c WHERE c.hikingSpot.id = :hikingSpotId AND c.status = :status ORDER BY c.createdAt DESC")
    List<Comment> findVisibleCommentsByHikingSpotId(@Param("hikingSpotId") int hikingSpotId, @Param("status") CommentStatus status);
    
    // Tous les commentaires d'un hiking spot (pour le propriétaire)
    List<Comment> findByHikingSpotId(int hikingSpotId);
    
    // Moyenne des notes d'un spot
    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.spot.id = :spotId AND c.status = 'VISIBLE'")
    Double getAverageRatingBySpotId(@Param("spotId") int spotId);
    
    // Moyenne des notes d'un hiking spot
    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.hikingSpot.id = :hikingSpotId AND c.status = 'VISIBLE'")
    Double getAverageRatingByHikingSpotId(@Param("hikingSpotId") int hikingSpotId);
    
    // Vérifier si un utilisateur a déjà commenté un spot
    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.user.id = :userId AND c.spot.id = :spotId")
    boolean hasUserCommentedOnSpot(@Param("userId") int userId, @Param("spotId") int spotId);
    
    // Vérifier si un utilisateur a déjà commenté un hiking spot
    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.user.id = :userId AND c.hikingSpot.id = :hikingSpotId")
    boolean hasUserCommentedOnHikingSpot(@Param("userId") int userId, @Param("hikingSpotId") int hikingSpotId);
}