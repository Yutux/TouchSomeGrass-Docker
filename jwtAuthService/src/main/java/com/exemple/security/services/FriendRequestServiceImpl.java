package com.exemple.security.services;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.FriendRequestDTO;
import com.exemple.security.entities.FriendRequest;
import com.exemple.security.entities.FriendRequest.RequestStatus;
import com.exemple.security.entities.UserApp;
import com.exemple.security.repositories.FriendRequestRepository;
import com.exemple.security.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final UserRelationService userRelationService;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<Map<String, Object>> sendFriendRequestFromAuth(String authHeader, Integer receiverId) {
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

            UserApp sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            FriendRequestDTO request = createFriendRequest(sender.getId(), receiverId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Friend request sent successfully");
            response.put("request", request);

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
    public ResponseEntity<Map<String, Object>> acceptFriendRequestFromAuth(String authHeader, Integer requestId) {
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

            acceptRequest(requestId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Friend request accepted successfully");

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
    public ResponseEntity<Map<String, Object>> declineFriendRequestFromAuth(String authHeader, Integer requestId) {
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

            declineRequest(requestId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Friend request declined successfully");

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
    public ResponseEntity<Map<String, Object>> cancelFriendRequestFromAuth(String authHeader, Integer requestId) {
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

            cancelRequest(requestId, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Friend request cancelled successfully");

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
    public ResponseEntity<Map<String, Object>> getPendingReceivedRequestsFromAuth(String authHeader) {
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

            List<FriendRequestDTO> requests = getPendingReceivedRequests(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requests", requests);
            response.put("count", requests.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> countPendingReceivedRequestsFromAuth(String authHeader) {
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

            long count = friendRequestRepository
            .countPendingReceivedByUserId(
                user.getId(),
                FriendRequest.RequestStatus.PENDING
            );


            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getSentRequestsFromAuth(String authHeader) {
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

            List<FriendRequestDTO> requests = getSentRequests(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requests", requests);
            response.put("count", requests.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    // ===== MÉTHODES INTERNES =====

    @Override
    public FriendRequestDTO createFriendRequest(Integer senderId, Integer receiverId) {
        // Vérifier que les utilisateurs existent
        UserApp sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        UserApp receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        // Vérifier qu'on n'envoie pas une demande à soi-même
        if (senderId.equals(receiverId)) {
            throw new IllegalStateException("Cannot send friend request to yourself");
        }
        
        // Vérifier qu'une demande PENDING n'existe pas déjà (dans un sens ou l'autre)
        if (friendRequestRepository.existsPendingRequest(senderId, receiverId)) {
            throw new IllegalStateException("A pending friend request already exists between these users");
        }
        
        // Vérifier qu'ils ne sont pas déjà amis
        // Note: Cette vérification dépend de votre implémentation de UserRelationService
        // Pour l'instant, on suppose qu'on peut l'ajouter après
        
        // Créer la demande
        FriendRequest request = FriendRequest.builder()
            .sender(sender)
            .receiver(receiver)
            .status(RequestStatus.PENDING)
            .build();
        
        FriendRequest savedRequest = friendRequestRepository.save(request);
        
        return convertToDTO(savedRequest);
    }

    @Override
    public void acceptRequest(Integer requestId, Integer userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Friend request not found"));
        
        // Vérifier que c'est bien le receiver qui accepte
        if (request.getReceiver().getId() != userId) {
            throw new IllegalStateException("You are not authorized to accept this request");
        }
        
        // Vérifier que la demande est en attente
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request is no longer pending");
        }
        
        // Mettre à jour le statut
        request.setStatus(RequestStatus.ACCEPTED);
        friendRequestRepository.save(request);
        
        // Ajouter la relation d'amitié (utilise votre UserRelationService existant)
        // Les deux utilisateurs deviennent amis mutuellement
        userRelationService.addFriend(request.getSender().getId(), request.getReceiver().getId());
        userRelationService.addFriend(request.getReceiver().getId(), request.getSender().getId());
    }

    @Override
    public void declineRequest(Integer requestId, Integer userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Friend request not found"));
        
        // Vérifier que c'est bien le receiver qui refuse
        if (request.getReceiver().getId() != userId) {
            throw new IllegalStateException("You are not authorized to decline this request");
        }
        
        // Vérifier que la demande est en attente
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request is no longer pending");
        }
        
        // Mettre à jour le statut
        request.setStatus(RequestStatus.DECLINED);
        friendRequestRepository.save(request);
    }

    @Override
    public void cancelRequest(Integer requestId, Integer userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Friend request not found"));
        
        // Vérifier que c'est bien le sender qui annule
        if (request.getSender().getId() != userId) {
            throw new IllegalStateException("You are not authorized to cancel this request");
        }
        
        // Supprimer la demande
        friendRequestRepository.delete(request);
    }

    @Override
    public List<FriendRequestDTO> getPendingReceivedRequests(Integer userId) {
        List<FriendRequest> requests = friendRequestRepository.findPendingReceivedByUserId(userId);
        return requests.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public long countPendingReceivedRequests(Integer userId) {
        return friendRequestRepository.countPendingReceivedByUserId(userId, FriendRequest.RequestStatus.PENDING);
    }

    @Override
    public List<FriendRequestDTO> getSentRequests(Integer userId) {
        List<FriendRequest> requests = friendRequestRepository.findSentByUserId(userId);
        return requests.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // ===== MÉTHODE UTILITAIRE =====

    private FriendRequestDTO convertToDTO(FriendRequest request) {
        return FriendRequestDTO.builder()
            .id(request.getId())
            .senderId(request.getSender().getId())
            .senderFirstname(request.getSender().getFirstname())
            .senderLastname(request.getSender().getLastname())
            .senderEmail(request.getSender().getEmail())
            //.senderAvatar(request.getSender().getAvatar())
            .receiverId(request.getReceiver().getId())
            .receiverFirstname(request.getReceiver().getFirstname())
            .receiverLastname(request.getReceiver().getLastname())
            .receiverEmail(request.getReceiver().getEmail())
            //.receiverAvatar(request.getReceiver().getAvatar())
            .status(request.getStatus())
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt())
            .build();
    }
}