package com.exemple.security.web;

import com.exemple.security.services.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    /**
     * Envoyer une demande d'ami
     * POST /api/v1/friend-requests/{receiverId}
     */
    @PostMapping("/{receiverId}")
    public ResponseEntity<?> sendFriendRequest(
            @PathVariable Integer receiverId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return friendRequestService.sendFriendRequestFromAuth(authHeader, receiverId);
    }

    /**
     * Accepter une demande d'ami
     * POST /api/v1/friend-requests/{requestId}/accept
     */
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(
            @PathVariable Integer requestId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return friendRequestService.acceptFriendRequestFromAuth(authHeader, requestId);
    }

    /**
     * Refuser une demande d'ami
     * POST /api/v1/friend-requests/{requestId}/decline
     */
    @PostMapping("/{requestId}/decline")
    public ResponseEntity<?> declineFriendRequest(
            @PathVariable Integer requestId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return friendRequestService.declineFriendRequestFromAuth(authHeader, requestId);
    }

    /**
     * Annuler une demande d'ami envoyée
     * DELETE /api/v1/friend-requests/{requestId}
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> cancelFriendRequest(
            @PathVariable Integer requestId,
            @RequestHeader("Authorization") String authHeader
    ) {
        return friendRequestService.cancelFriendRequestFromAuth(authHeader, requestId);
    }

    /**
     * Obtenir les demandes d'ami reçues (PENDING)
     * GET /api/v1/friend-requests/received
     */
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedFriendRequests(
            @RequestHeader("Authorization") String authHeader
    ) {
        return friendRequestService.getPendingReceivedRequestsFromAuth(authHeader);
    }

    /**
     * Compter les demandes d'ami reçues (PENDING)
     * GET /api/v1/friend-requests/received/count
     */
    @GetMapping("/received/count")
    public ResponseEntity<?> countReceivedFriendRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        return friendRequestService.countPendingReceivedRequestsFromAuth(authHeader);
    }

    /**
     * Obtenir les demandes d'ami envoyées
     * GET /api/v1/friend-requests/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<?> getSentFriendRequests(
            @RequestHeader("Authorization") String authHeader
    ) {
        return friendRequestService.getSentRequestsFromAuth(authHeader);
    }
}