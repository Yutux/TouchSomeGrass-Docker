package com.exemple.security.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exemple.security.entities.Conversation;
import com.exemple.security.enums.ConversationType;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    
    // Conversations d'un utilisateur (privées)
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId AND c.type = 'PRIVATE' ORDER BY c.lastMessageAt DESC")
    List<Conversation> findPrivateConversationsByUserId(@Param("userId") int userId);
    
    // Conversations d'un groupe
    List<Conversation> findByGroupIdOrderByCreatedAtDesc(int groupId);
    
    // Conversation privée entre deux utilisateurs
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE p1.id = :userId1 AND p2.id = :userId2 AND c.type = 'PRIVATE' AND c.group IS NULL")
    Optional<Conversation> findPrivateConversationBetweenUsers(@Param("userId1") int userId1, @Param("userId2") int userId2);
    
    // Toutes les conversations d'un utilisateur (privées + groupes)
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "LEFT JOIN c.participants p " +
           "LEFT JOIN c.group g " +
           "LEFT JOIN g.memberships m " +
           "WHERE (p.id = :userId AND c.type = 'PRIVATE') OR (m.user.id = :userId AND c.type = 'GROUP') " +
           "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllUserConversations(@Param("userId") int userId);
}