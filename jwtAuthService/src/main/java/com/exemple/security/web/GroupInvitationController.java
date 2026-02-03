package com.exemple.security.web;

import com.exemple.security.services.GroupInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-relations/groups")
@RequiredArgsConstructor
public class GroupInvitationController {

    private final GroupInvitationService invitationService;

    @PostMapping("/{groupId}/invitations")
    public ResponseEntity<?> sendInvitation(
            @PathVariable Integer groupId,
            @RequestBody Map<String, Integer> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        Integer userId = body.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        return invitationService.sendInvitationFromAuth(authHeader, groupId, userId);
    }

    @PutMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Integer invitationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.acceptInvitationFromAuth(authHeader, invitationId);
    }

    @PutMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<?> declineInvitation(
            @PathVariable Integer invitationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.declineInvitationFromAuth(authHeader, invitationId);
    }

    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<?> cancelInvitation(
            @PathVariable Integer invitationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.cancelInvitationFromAuth(authHeader, invitationId);
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<?> getPendingInvitations(
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.getPendingInvitationsFromAuth(authHeader);
    }

    @GetMapping("/invitations/pending/count")
    public ResponseEntity<?> countPendingInvitations(
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.countPendingInvitationsFromAuth(authHeader);
    }

    @GetMapping("/{groupId}/invitations")
    public ResponseEntity<?> getGroupInvitations(
            @PathVariable Integer groupId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.getGroupInvitationsFromAuth(authHeader, groupId);
    }

    @GetMapping("/invitations/sent")
    public ResponseEntity<?> getSentInvitations(
            @RequestHeader("Authorization") String authHeader
    ) {
        return invitationService.getSentInvitationsFromAuth(authHeader);
    }
}