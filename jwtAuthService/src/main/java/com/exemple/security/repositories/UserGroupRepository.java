package com.exemple.security.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exemple.security.entities.UserGroup;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {
    
    // Groupes créés par un utilisateur
    List<UserGroup> findByCreatorId(int creatorId);
    
    // Groupes dont l'utilisateur est membre
    @Query("SELECT g FROM UserGroup g JOIN g.memberships m WHERE m.user.id = :userId ORDER BY g.createdAt DESC")
    List<UserGroup> findGroupsByUserId(@Param("userId") int userId);
    
    // Groupes publics
    @Query("SELECT g FROM UserGroup g WHERE g.isPrivate = false ORDER BY g.createdAt DESC")
    List<UserGroup> findPublicGroups();
    
    // Rechercher des groupes par nom
    @Query("SELECT g FROM UserGroup g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')) AND g.isPrivate = false")
    List<UserGroup> searchPublicGroupsByName(@Param("name") String name);
}