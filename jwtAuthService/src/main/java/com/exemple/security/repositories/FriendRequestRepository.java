package com.exemple.security.repositories;

import com.exemple.security.entities.FriendRequest;
import com.exemple.security.entities.FriendRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

    /**
     * Trouver une demande par senderId et receiverId
     */
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.sender.id = :senderId AND fr.receiver.id = :receiverId")
    Optional<FriendRequest> findBySenderIdAndReceiverId(
            @Param("senderId") Integer senderId,
            @Param("receiverId") Integer receiverId
    );

    /**
     * Vérifier si une demande PENDING existe déjà entre 2 utilisateurs (dans un sens ou l'autre)
     */
    @Query("SELECT COUNT(fr) > 0 FROM FriendRequest fr " +
           "WHERE ((fr.sender.id = :userId1 AND fr.receiver.id = :userId2) " +
           "OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1)) " +
           "AND fr.status = 'PENDING'")
    boolean existsPendingRequest(
            @Param("userId1") Integer userId1,
            @Param("userId2") Integer userId2
    );

    /**
     * Trouver toutes les demandes reçues par un utilisateur avec un certain statut
     */
    @Query("SELECT fr FROM FriendRequest fr " +
           "LEFT JOIN FETCH fr.sender s " +
           "WHERE fr.receiver.id = :receiverId AND fr.status = :status " +
           "ORDER BY fr.createdAt DESC")
    List<FriendRequest> findReceivedByUserIdAndStatus(
            @Param("receiverId") Integer receiverId,
            @Param("status") RequestStatus status
    );

    /**
     * Trouver toutes les demandes PENDING reçues par un utilisateur
     */
    @Query("SELECT fr FROM FriendRequest fr " +
           "LEFT JOIN FETCH fr.sender s " +
           "WHERE fr.receiver.id = :receiverId AND fr.status = 'PENDING' " +
           "ORDER BY fr.createdAt DESC")
    List<FriendRequest> findPendingReceivedByUserId(@Param("receiverId") Integer receiverId);

    /**
     * Compter les demandes PENDING reçues par un utilisateur
     */
    @Query("""
    SELECT COUNT(fr)
    FROM FriendRequest fr
    WHERE fr.receiver.id = :receiverId
      AND fr.status = :status
    """)
    long countPendingReceivedByUserId(
        @Param("receiverId") Integer receiverId,
        @Param("status") FriendRequest.RequestStatus status
    );


    /**
     * Trouver toutes les demandes envoyées par un utilisateur
     */
    @Query("SELECT fr FROM FriendRequest fr " +
           "LEFT JOIN FETCH fr.receiver r " +
           "WHERE fr.sender.id = :senderId " +
           "ORDER BY fr.createdAt DESC")
    List<FriendRequest> findSentByUserId(@Param("senderId") Integer senderId);

    /**
     * Trouver toutes les demandes envoyées par un utilisateur avec un certain statut
     */
    @Query("SELECT fr FROM FriendRequest fr " +
           "LEFT JOIN FETCH fr.receiver r " +
           "WHERE fr.sender.id = :senderId AND fr.status = :status " +
           "ORDER BY fr.createdAt DESC")
    List<FriendRequest> findSentByUserIdAndStatus(
            @Param("senderId") Integer senderId,
            @Param("status") RequestStatus status
    );
}