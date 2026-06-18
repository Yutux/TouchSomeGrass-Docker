package com.exemple.security.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exemple.security.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    
    // Messages d'une conversation
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.sentAt ASC")
    List<Message> findByConversationId(@Param("conversationId") int conversationId);
    
    // Messages envoyés par un utilisateur
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId ORDER BY m.sentAt DESC")
    List<Message> findBySenderId(@Param("senderId") int senderId);
    
    // Messages non lus d'un utilisateur dans toutes ses conversations
    @Query("SELECT m FROM Message m JOIN m.conversation c JOIN c.participants p " +
           "WHERE p.id = :userId AND m.sender.id != :userId AND m.isRead = false ORDER BY m.sentAt DESC")
    List<Message> findUnreadMessagesByUserId(@Param("userId") int userId);
    
    // Nombre de messages non lus par conversation pour un utilisateur
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId AND m.isRead = false")
    long countUnreadMessagesInConversation(@Param("conversationId") int conversationId, @Param("userId") int userId);
    
    // Dernier message d'une conversation
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.sentAt DESC LIMIT 1")
    Message findLastMessageInConversation(@Param("conversationId") int conversationId);
    
    // Nombre total de messages non lus pour un utilisateur
    @Query("SELECT COUNT(m) FROM Message m JOIN m.conversation c JOIN c.participants p " +
           "WHERE p.id = :userId AND m.sender.id != :userId AND m.isRead = false")
    long countUnreadMessagesByUserId(@Param("userId") int userId);
}