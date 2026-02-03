package com.exemple.security.services;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.GroupInvitationDTO;
import com.exemple.security.entities.GroupInvitation;
import com.exemple.security.entities.UserApp;
import com.exemple.security.entities.UserGroup;
import com.exemple.security.entities.GroupInvitation.InvitationStatus;
import com.exemple.security.repositories.GroupInvitationRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.repositories.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupInvitationServiceImpl implements GroupInvitationService {

    private final GroupInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository groupRepository;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<Map<String, Object>> sendInvitationFromAuth(String authHeader, int groupId, int userId) {
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

            UserApp inviter = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            GroupInvitationDTO invitation = createInvitation(groupId, userId, inviter.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation sent successfully");
            response.put("invitation", invitation);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> acceptInvitationFromAuth(String authHeader, int invitationId) {
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

            acceptInvitation(invitationId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation accepted successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> declineInvitationFromAuth(String authHeader, int invitationId) {
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

            declineInvitation(invitationId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation declined successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> cancelInvitationFromAuth(String authHeader, int invitationId) {
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

            cancelInvitation(invitationId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getPendingInvitationsFromAuth(String authHeader) {
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

            List<GroupInvitationDTO> invitations = getPendingInvitationsForUser(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("invitations", invitations);
            response.put("count", invitations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> countPendingInvitationsFromAuth(String authHeader) {
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

            long count = countPendingInvitationsForUser(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getGroupInvitationsFromAuth(String authHeader, int groupId) {
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

            List<GroupInvitationDTO> invitations = getGroupInvitations(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("invitations", invitations);
            response.put("count", invitations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getSentInvitationsFromAuth(String authHeader) {
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

            List<GroupInvitationDTO> invitations = getSentInvitationsByUser(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("invitations", invitations);
            response.put("count", invitations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    // ===== MÉTHODES INTERNES =====

    @Override
    public GroupInvitationDTO createInvitation(int groupId, int userId, int invitedById) {
        // Vérifier que le groupe existe
        UserGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Vérifier que l'utilisateur à inviter existe
        UserApp userToInvite = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Vérifier que l'inviteur existe
        UserApp inviter = userRepository.findById(invitedById)
            .orElseThrow(() -> new RuntimeException("Inviter not found"));
        
        // Vérifier qu'une invitation en attente n'existe pas déjà
        if (invitationRepository.existsPendingInvitation(userId, groupId)) {
            throw new IllegalStateException("A pending invitation already exists for this user and group");
        }
        
        // Créer l'invitation
        GroupInvitation invitation = GroupInvitation.builder()
            .user(userToInvite)
            .group(group)
            .invitedBy(inviter)
            .status(InvitationStatus.PENDING)
            .build();
        
        GroupInvitation savedInvitation = invitationRepository.save(invitation);
        
        // Convertir en DTO
        return convertToDTO(savedInvitation);
    }

    @Override
    public void acceptInvitation(int invitationId, int userId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Vérifier que c'est bien l'utilisateur invité
        if (invitation.getUser().getId() != userId) {
            throw new IllegalStateException("You are not authorized to accept this invitation");
        }
        
        // Vérifier que l'invitation est en attente
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("This invitation is no longer pending");
        }
        
        // Mettre à jour le statut
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        
        // TODO: Ajouter l'utilisateur au groupe via GroupService
        // groupService.addMemberToGroup(invitation.getGroup().getId(), userId, userId, GroupRole.MEMBER);
    }

    @Override
    public void declineInvitation(int invitationId, int userId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Vérifier que c'est bien l'utilisateur invité
        if (invitation.getUser().getId() != userId) {
            throw new IllegalStateException("You are not authorized to decline this invitation");
        }
        
        // Vérifier que l'invitation est en attente
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("This invitation is no longer pending");
        }
        
        // Mettre à jour le statut
        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }

    @Override
    public void cancelInvitation(int invitationId, int userId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Vérifier que c'est bien l'inviteur ou un admin du groupe
        if (invitation.getInvitedBy().getId() != userId) {
            // TODO: Vérifier si l'utilisateur est admin du groupe
            throw new IllegalStateException("You are not authorized to cancel this invitation");
        }
        
        // Supprimer l'invitation
        invitationRepository.delete(invitation);
    }

    @Override
    public List<GroupInvitationDTO> getPendingInvitationsForUser(int userId) {
        List<GroupInvitation> invitations = invitationRepository.findPendingInvitationsByUserId(userId);
        return invitations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public long countPendingInvitationsForUser(int userId) {
        return invitationRepository.countPendingInvitationsByUserId(userId);
    }

    @Override
    public List<GroupInvitationDTO> getGroupInvitations(int groupId) {
        List<GroupInvitation> invitations = invitationRepository.findByGroupId(groupId);
        return invitations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<GroupInvitationDTO> getSentInvitationsByUser(int userId) {
        List<GroupInvitation> invitations = invitationRepository.findSentInvitationsByUserId(userId);
        return invitations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // ===== MÉTHODE UTILITAIRE =====

    private GroupInvitationDTO convertToDTO(GroupInvitation invitation) {
        return GroupInvitationDTO.builder()
            .id(invitation.getId())
            .groupId(invitation.getGroup().getId())
            .groupName(invitation.getGroup().getName())
            //.groupImageUrl(invitation.getGroup().getImageUrl())
            .userId(invitation.getUser().getId())
            //.userFirstName(invitation.getUser().getFirstname())
            //.userLastName(invitation.getUser().getLastname())
            .invitedById(invitation.getInvitedBy().getId())
            //.invitedByFirstName(invitation.getInvitedBy().getFirstname())
            //.invitedByLastName(invitation.getInvitedBy().getLastname())
            .status(invitation.getStatus())
            .createdAt(invitation.getCreatedAt())
            .build();
    }
}