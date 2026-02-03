package com.exemple.security.repositories;

import com.exemple.security.entities.GroupInvitation;
import com.exemple.security.entities.GroupInvitation.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Integer> {

    /**
     * Trouver une invitation par userId, groupId et status
     */
    @Query("SELECT i FROM GroupInvitation i WHERE i.user.id = :userId AND i.group.id = :groupId AND i.status = :status")
    Optional<GroupInvitation> findByUserIdAndGroupIdAndStatus(
            @Param("userId") int userId,
            @Param("groupId") int groupId,
            @Param("status") InvitationStatus status
    );

    /**
     * Trouver toutes les invitations d'un utilisateur avec un certain status
     */
    @Query("SELECT i FROM GroupInvitation i " +
           "LEFT JOIN FETCH i.group g " +
           "LEFT JOIN FETCH i.invitedBy ib " +
           "WHERE i.user.id = :userId AND i.status = :status " +
           "ORDER BY i.createdAt DESC")
    List<GroupInvitation> findByUserIdAndStatus(
            @Param("userId") int userId,
            @Param("status") InvitationStatus status
    );

    /**
     * Trouver toutes les invitations en attente pour un utilisateur
     */
    @Query("SELECT i FROM GroupInvitation i " +
           "LEFT JOIN FETCH i.group g " +
           "LEFT JOIN FETCH i.invitedBy ib " +
           "WHERE i.user.id = :userId AND i.status = 'PENDING' " +
           "ORDER BY i.createdAt DESC")
    List<GroupInvitation> findPendingInvitationsByUserId(@Param("userId") int userId);

    /**
     * Trouver toutes les invitations d'un groupe
     */
    @Query("SELECT i FROM GroupInvitation i " +
           "LEFT JOIN FETCH i.user u " +
           "WHERE i.group.id = :groupId " +
           "ORDER BY i.createdAt DESC")
    List<GroupInvitation> findByGroupId(@Param("groupId") int groupId);

    /**
     * Vérifier si une invitation en attente existe déjà
     */
    @Query("SELECT COUNT(i) > 0 FROM GroupInvitation i " +
           "WHERE i.user.id = :userId AND i.group.id = :groupId AND i.status = 'PENDING'")
    boolean existsPendingInvitation(
            @Param("userId") int userId,
            @Param("groupId") int groupId
    );

    /**
     * ✅ CORRIGÉ : Compter les invitations en attente pour un utilisateur
     */
    @Query("SELECT COUNT(i) FROM GroupInvitation i WHERE i.user.id = :userId AND i.status = 'PENDING'")
    long countPendingInvitationsByUserId(@Param("userId") int userId);

    /**
     * Supprimer toutes les invitations d'un groupe
     */
    void deleteByGroupId(int groupId);

    /**
     * Supprimer toutes les invitations d'un utilisateur dans un groupe
     */
    @Query("DELETE FROM GroupInvitation i WHERE i.user.id = :userId AND i.group.id = :groupId")
    void deleteByUserIdAndGroupId(
            @Param("userId") int userId,
            @Param("groupId") int groupId
    );

    /**
     * Trouver les invitations envoyées par un utilisateur
     */
    @Query("SELECT i FROM GroupInvitation i " +
           "LEFT JOIN FETCH i.user u " +
           "LEFT JOIN FETCH i.group g " +
           "WHERE i.invitedBy.id = :userId " +
           "ORDER BY i.createdAt DESC")
    List<GroupInvitation> findSentInvitationsByUserId(@Param("userId") Integer userId);

    // ❌ SUPPRIMÉ : Cette méthode causait l'erreur
    // long countByInvitedUserIdAndStatus(Integer invitedUserId, InvitationStatus status);
}