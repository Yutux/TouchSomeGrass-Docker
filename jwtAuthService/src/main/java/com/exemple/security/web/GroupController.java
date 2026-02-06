package com.exemple.security.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.exemple.security.dtos.CreateGroupDto;
import com.exemple.security.dtos.GroupResponseDto;
import com.exemple.security.enums.GroupRole;
import com.exemple.security.services.GroupService;

import lombok.RequiredArgsConstructor;

/**
 * Controller dédié à la gestion des groupes
 * Séparé pour éviter les conflits de routes avec UserRelationController
 */
@RestController
@RequestMapping("/api/v1/groupSource")
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;
    
    // ========================================
    // 🔓 ENDPOINTS PUBLICS (SANS AUTH)
    // ========================================
    
    /**
     * GET /api/v1/groupSource/public
     * Récupérer tous les groupes publics
     */
    @GetMapping("/public")
    public ResponseEntity<GroupResponseDto> getPublicGroups() {
        return groupService.getPublicGroups();
    }
    
    /**
     * GET /api/v1/groupSource/search?name=xxx
     * Rechercher des groupes publics par nom
     */
    @GetMapping("/search")
    public ResponseEntity<GroupResponseDto> searchPublicGroups(@RequestParam String name) {
        return groupService.searchPublicGroups(name);
    }
    
    /**
     * GET /api/v1/groupSource/details/{groupId}
     * Récupérer un groupe par son ID (public ou privé)
     */
    @GetMapping("/details/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroupById(@PathVariable int groupId) {
        return groupService.getGroupById(groupId);
    }
    
    // ========================================
    // 🔐 ENDPOINTS PRIVÉS (AVEC AUTH)
    // ========================================
    
    /**
     * POST /api/v1/groupSource/create
     * Créer un nouveau groupe
     */
    @PostMapping("/create")
    public ResponseEntity<GroupResponseDto> createGroup(
            @RequestBody CreateGroupDto request,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.createGroupFromAuth(authHeader, request);
    }
    
    /**
     * GET /api/v1/groupSource/my-groups
     * Récupérer MES groupes (public + privé)
     */
    @GetMapping("/my-groups")
    public ResponseEntity<GroupResponseDto> getUserGroups(
            @RequestHeader("Authorization") String authHeader) {
        return groupService.getUserGroupsFromAuth(authHeader);
    }
    
    /**
     * DELETE /api/v1/groupSource/delete/{groupId}
     * Supprimer un groupe (owner seulement)
     */
    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<GroupResponseDto> deleteGroup(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.deleteGroupFromAuth(authHeader, groupId);
    }
    
    // ========================================
    // 🔐 GESTION DES MEMBRES
    // ========================================
    
    /**
     * GET /api/v1/groupSource/{groupId}/members
     * Récupérer les membres d'un groupe
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.getGroupMembersFromAuth(authHeader, groupId);
    }
    
    /**
     * POST /api/v1/groupSource/{groupId}/members/add/{userId}
     * Ajouter un membre au groupe
     */
    @PostMapping("/{groupId}/members/add/{userId}")
    public ResponseEntity<GroupResponseDto> addMemberToGroup(
            @PathVariable int groupId,
            @PathVariable int userId,
            @RequestParam(required = false) GroupRole role,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.addMemberToGroupFromAuth(authHeader, groupId, userId, role);
    }
    
    /**
     * DELETE /api/v1/groupSource/{groupId}/members/remove/{userId}
     * Retirer un membre du groupe
     */
    @DeleteMapping("/{groupId}/members/remove/{userId}")
    public ResponseEntity<GroupResponseDto> removeMemberFromGroup(
            @PathVariable int groupId,
            @PathVariable int userId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.removeMemberFromGroupFromAuth(authHeader, groupId, userId);
    }
    
    /**
     * PUT /api/v1/groupSource/{groupId}/members/{userId}/role
     * Changer le rôle d'un membre
     */
    @PutMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<GroupResponseDto> changeMemberRole(
            @PathVariable int groupId,
            @PathVariable int userId,
            @RequestParam GroupRole newRole,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.changeMemberRoleFromAuth(authHeader, groupId, userId, newRole);
    }
    
    /**
     * POST /api/v1/groupSource/{groupId}/leave
     * Quitter un groupe
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<GroupResponseDto> leaveGroup(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.leaveGroupFromAuth(authHeader, groupId);
    }
}