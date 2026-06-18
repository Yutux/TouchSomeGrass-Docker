package com.exemple.security.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.CreateGroupDto;
import com.exemple.security.dtos.GroupResponseDto;
import com.exemple.security.entities.GroupMembership;
import com.exemple.security.entities.UserApp;
import com.exemple.security.entities.UserGroup;
import com.exemple.security.enums.GroupRole;
import com.exemple.security.repositories.GroupMembershipRepository;
import com.exemple.security.repositories.UserGroupRepository;
import com.exemple.security.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    
    private final UserGroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository groupMemberRepository;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<GroupResponseDto> createGroupFromAuth(String authHeader, CreateGroupDto request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return createGroup(creator.getId(), request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    
    @Override
    public ResponseEntity<?> getGroupMembersFromAuth(String authHeader, int groupId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Vérifier que le groupe existe
            UserGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Récupérer les membres
            List<GroupMembership> members = groupMemberRepository.findByGroupId(groupId);
            
            // Convertir en DTO
            List<Map<String, Object>> memberDtos = members.stream()
                .map(member -> {
                    UserApp memberUser = member.getUser();
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", memberUser.getId());
                    dto.put("firstname", memberUser.getFirstname());
                    dto.put("lastname", memberUser.getLastname());
                    dto.put("email", memberUser.getEmail());
                    dto.put("role", member.getRole().name());
                    
                    return dto;
                })
                .collect(Collectors.toList());

            // Retour direct avec Map
            return ResponseEntity.ok(Map.of(
                "success", true,
                "members", memberDtos,
                "count", memberDtos.size()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<GroupResponseDto> getUserGroupsFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return getUserGroups(user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GroupResponseDto> addMemberToGroupFromAuth(String authHeader, int groupId, int userId, GroupRole role) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return addMemberToGroup(groupId, userId, requester.getId(), role);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GroupResponseDto> removeMemberFromGroupFromAuth(String authHeader, int groupId, int userId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return removeMemberFromGroup(groupId, userId, requester.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GroupResponseDto> changeMemberRoleFromAuth(String authHeader, int groupId, int userId, GroupRole newRole) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return changeMemberRole(groupId, userId, requester.getId(), newRole);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GroupResponseDto> leaveGroupFromAuth(String authHeader, int groupId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return leaveGroup(groupId, user.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GroupResponseDto> deleteGroupFromAuth(String authHeader, int groupId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GroupResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return deleteGroup(groupId, owner.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GroupResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    // ===== MÉTHODES PUBLIQUES (INCHANGÉES) =====

    @Override
    public ResponseEntity<GroupResponseDto> getGroupById(int groupId) {
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Groupe trouvé")
                .group(group)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> getPublicGroups() {
        List<UserGroup> groups = groupRepository.findPublicGroups();
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Groupes publics récupérés")
                .groups(groups)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> searchPublicGroups(String name) {
        List<UserGroup> groups = groupRepository.searchPublicGroupsByName(name);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Résultats de recherche")
                .groups(groups)
                .build());
    }

    // ===== MÉTHODES INTERNES (INCHANGÉES) =====

    @Override
    public ResponseEntity<GroupResponseDto> createGroup(int creatorId, CreateGroupDto request) {
        UserApp creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Le nom du groupe ne peut pas être vide")
                    .build());
        }
        
        UserGroup group = UserGroup.builder()
            .name(request.getName())
            .description(request.getDescription())
            .imageUrl(request.getImageUrl())
            .isPrivate(request.isPrivate())
            .creator(creator)
            .build();
        
        UserGroup savedGroup = groupRepository.save(group);
        
        GroupMembership membership = GroupMembership.builder()
            .user(creator)
            .group(savedGroup)
            .role(GroupRole.OWNER)
            .build();
        
        membershipRepository.save(membership);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Groupe créé avec succès")
                .group(savedGroup)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> getUserGroups(int userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        List<UserGroup> groups = groupRepository.findGroupsByUserId(userId);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Groupes de l'utilisateur récupérés")
                .groups(groups)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> addMemberToGroup(int groupId, int userId, int requesterId, GroupRole role) {
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        GroupMembership requesterMembership = membershipRepository.findByUserIdAndGroupId(requesterId, groupId)
            .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce groupe"));
        
        if (requesterMembership.getRole() != GroupRole.OWNER && requesterMembership.getRole() != GroupRole.ADMIN) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Vous n'avez pas la permission d'ajouter des membres")
                    .build());
        }
        
        if (membershipRepository.isUserMemberOfGroup(userId, groupId)) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("L'utilisateur est déjà membre du groupe")
                    .build());
        }
        
        GroupMembership membership = GroupMembership.builder()
            .user(user)
            .group(group)
            .role(role != null ? role : GroupRole.MEMBER)
            .build();
        
        membershipRepository.save(membership);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Membre ajouté avec succès")
                .group(group)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> removeMemberFromGroup(int groupId, int userId, int requesterId) {
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        GroupMembership requesterMembership = membershipRepository.findByUserIdAndGroupId(requesterId, groupId)
            .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce groupe"));
        
        GroupMembership memberToRemove = membershipRepository.findByUserIdAndGroupId(userId, groupId)
            .orElseThrow(() -> new RuntimeException("L'utilisateur n'est pas membre du groupe"));
        
        if (requesterMembership.getRole() != GroupRole.OWNER && requesterMembership.getRole() != GroupRole.ADMIN) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Vous n'avez pas la permission de retirer des membres")
                    .build());
        }
        
        if (memberToRemove.getRole() == GroupRole.OWNER) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Impossible de retirer le propriétaire du groupe")
                    .build());
        }
        
        membershipRepository.delete(memberToRemove);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Membre retiré avec succès")
                .group(group)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> changeMemberRole(int groupId, int userId, int requesterId, GroupRole newRole) {
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        GroupMembership requesterMembership = membershipRepository.findByUserIdAndGroupId(requesterId, groupId)
            .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce groupe"));
        
        if (requesterMembership.getRole() != GroupRole.OWNER) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Seul le propriétaire peut changer les rôles")
                    .build());
        }
        
        GroupMembership memberToUpdate = membershipRepository.findByUserIdAndGroupId(userId, groupId)
            .orElseThrow(() -> new RuntimeException("L'utilisateur n'est pas membre du groupe"));
        
        if (memberToUpdate.getRole() == GroupRole.OWNER) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Impossible de changer le rôle du propriétaire")
                    .build());
        }
        
        memberToUpdate.setRole(newRole);
        membershipRepository.save(memberToUpdate);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Rôle du membre modifié avec succès")
                .group(group)
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> leaveGroup(int groupId, int userId) {
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        GroupMembership membership = membershipRepository.findByUserIdAndGroupId(userId, groupId)
            .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce groupe"));
        
        if (membership.getRole() == GroupRole.OWNER) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Le propriétaire ne peut pas quitter le groupe. Supprimez le groupe ou transférez la propriété.")
                    .build());
        }
        
        membershipRepository.delete(membership);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Vous avez quitté le groupe avec succès")
                .build());
    }

    @Override
    public ResponseEntity<GroupResponseDto> deleteGroup(int groupId, int ownerId) {
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        
        GroupMembership membership = membershipRepository.findByUserIdAndGroupId(ownerId, groupId)
            .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce groupe"));
        
        if (membership.getRole() != GroupRole.OWNER) {
            return ResponseEntity.badRequest()
                .body(GroupResponseDto.builder()
                    .message("Seul le propriétaire peut supprimer le groupe")
                    .build());
        }
        
        groupRepository.delete(group);
        
        return ResponseEntity.ok()
            .body(GroupResponseDto.builder()
                .message("Groupe supprimé avec succès")
                .build());
    }

    @Override
    public boolean canUserCreateConversations(int userId, int groupId) {
        GroupMembership membership = membershipRepository.findByUserIdAndGroupId(userId, groupId)
            .orElse(null);
        
        if (membership == null) {
            return false;
        }
        
        return membership.getRole() == GroupRole.OWNER || 
               membership.getRole() == GroupRole.ADMIN || 
               membership.getRole() == GroupRole.MODERATOR;
    }
}