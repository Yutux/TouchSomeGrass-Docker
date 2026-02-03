package com.exemple.security.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exemple.security.entities.GroupMembership;
import com.exemple.security.enums.GroupRole;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Integer> {
    
    // Trouver l'appartenance d'un utilisateur à un groupe
    Optional<GroupMembership> findByUserIdAndGroupId(int userId, int groupId);
    
    // Tous les membres d'un groupe
    List<GroupMembership> findByGroupId(int groupId);
    
    // Tous les groupes d'un utilisateur
    List<GroupMembership> findByUserId(int userId);
    
    // Membres avec un rôle spécifique dans un groupe
    List<GroupMembership> findByGroupIdAndRole(int groupId, GroupRole role);
    
    // Vérifier si un utilisateur est membre d'un groupe
    @Query("SELECT COUNT(m) > 0 FROM GroupMembership m WHERE m.user.id = :userId AND m.group.id = :groupId")
    boolean isUserMemberOfGroup(@Param("userId") int userId, @Param("groupId") int groupId);
    
    // Vérifier si un utilisateur a un rôle spécifique dans un groupe
    @Query("SELECT COUNT(m) > 0 FROM GroupMembership m WHERE m.user.id = :userId AND m.group.id = :groupId AND m.role = :role")
    boolean hasUserRoleInGroup(@Param("userId") int userId, @Param("groupId") int groupId, @Param("role") GroupRole role);

    boolean existsByUserIdAndGroupId(int userId, int groupId);
}