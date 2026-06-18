package com.exemple.security.services;

import org.springframework.http.ResponseEntity;
import com.exemple.security.dtos.CreateGroupDto;
import com.exemple.security.dtos.GroupResponseDto;
import com.exemple.security.enums.GroupRole;

public interface GroupService {

    // ===== MÉTHODES AVEC AUTHENTIFICATION (authHeader) =====
    
    ResponseEntity<GroupResponseDto> createGroupFromAuth(String authHeader, CreateGroupDto request);
    ResponseEntity<GroupResponseDto> getUserGroupsFromAuth(String authHeader);
    ResponseEntity<?> getGroupMembersFromAuth(String authHeader, int groupId);
    ResponseEntity<GroupResponseDto> addMemberToGroupFromAuth(String authHeader, int groupId, int userId, GroupRole role);
    ResponseEntity<GroupResponseDto> removeMemberFromGroupFromAuth(String authHeader, int groupId, int userId);
    ResponseEntity<GroupResponseDto> changeMemberRoleFromAuth(String authHeader, int groupId, int userId, GroupRole newRole);
    ResponseEntity<GroupResponseDto> leaveGroupFromAuth(String authHeader, int groupId);
    ResponseEntity<GroupResponseDto> deleteGroupFromAuth(String authHeader, int groupId);
    
    // ===== MÉTHODES PUBLIQUES (sans authentification) =====
    
    ResponseEntity<GroupResponseDto> getGroupById(int groupId);
    ResponseEntity<GroupResponseDto> getPublicGroups();
    ResponseEntity<GroupResponseDto> searchPublicGroups(String name);
    
    // ===== MÉTHODES INTERNES (utilisées par les méthodes FromAuth) =====
    
    ResponseEntity<GroupResponseDto> createGroup(int creatorId, CreateGroupDto request);
    ResponseEntity<GroupResponseDto> getUserGroups(int userId);
    ResponseEntity<GroupResponseDto> addMemberToGroup(int groupId, int userId, int requesterId, GroupRole role);
    ResponseEntity<GroupResponseDto> removeMemberFromGroup(int groupId, int userId, int requesterId);
    ResponseEntity<GroupResponseDto> changeMemberRole(int groupId, int userId, int requesterId, GroupRole newRole);
    ResponseEntity<GroupResponseDto> leaveGroup(int groupId, int userId);
    ResponseEntity<GroupResponseDto> deleteGroup(int groupId, int ownerId);
    
    boolean canUserCreateConversations(int userId, int groupId);
}