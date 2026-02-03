package com.exemple.security.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.AuthenticationResponseDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.repositories.HikingSpotRepository;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserRelationServiceImpl implements UserRelationService {
    
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;
    private final HikingSpotRepository hikingSpotRepository;
    
    @Autowired
    private JwtService jwtService;

    // ===== MÉTHODES AVEC AUTHENTIFICATION =====

    @Override
    public ResponseEntity<AuthenticationResponseDto> addFavoriteSpotFromAuth(String authHeader, int spotId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return addFavoriteSpot(user.getId(), spotId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthenticationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> removeFavoriteSpotFromAuth(String authHeader, int spotId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return removeFavoriteSpot(user.getId(), spotId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthenticationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<List<Spot>> getFavoriteSpotsFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return ResponseEntity.ok(getFavoriteSpots(user.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> addFavoriteHikingSpotFromAuth(String authHeader, int hikingSpotId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return addFavoriteHikingSpot(user.getId(), hikingSpotId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthenticationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> removeFavoriteHikingSpotFromAuth(String authHeader, int hikingSpotId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return removeFavoriteHikingSpot(user.getId(), hikingSpotId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthenticationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<List<HikingSpot>> getFavoriteHikingSpotsFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return ResponseEntity.ok(getFavoriteHikingSpots(user.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> addFriendFromAuth(String authHeader, int friendId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return addFriend(user.getId(), friendId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthenticationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> removeFriendFromAuth(String authHeader, int friendId) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Missing or invalid Authorization header")
                        .build());
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                        .message("Invalid token")
                        .build());
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return removeFriend(user.getId(), friendId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthenticationResponseDto.builder()
                    .message("Error processing request: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public ResponseEntity<List<UserApp>> getFriendsFromAuth(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserApp user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return ResponseEntity.ok(getFriends(user.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== MÉTHODES INTERNES =====

    @Override
    public ResponseEntity<AuthenticationResponseDto> addFavoriteSpot(int userId, int spotId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        Spot spot = spotRepository.findById(spotId)
            .orElseThrow(() -> new RuntimeException("Spot non trouvé"));
        
        if (user.getFavoriteSpots().contains(spot)) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Ce spot est déjà dans vos favoris")
                    .build());
        }
        
        user.getFavoriteSpots().add(spot);
        userRepository.save(user);
        
        return ResponseEntity.ok()
            .body(AuthenticationResponseDto.builder()
                .message("Spot ajouté aux favoris avec succès")
                .userApp(user)
                .build());
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> removeFavoriteSpot(int userId, int spotId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        Spot spot = spotRepository.findById(spotId)
            .orElseThrow(() -> new RuntimeException("Spot non trouvé"));
        
        if (!user.getFavoriteSpots().contains(spot)) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Ce spot n'est pas dans vos favoris")
                    .build());
        }
        
        user.getFavoriteSpots().remove(spot);
        userRepository.save(user);
        
        return ResponseEntity.ok()
            .body(AuthenticationResponseDto.builder()
                .message("Spot retiré des favoris avec succès")
                .userApp(user)
                .build());
    }

    @Override
    public List<Spot> getFavoriteSpots(int userId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        return user.getFavoriteSpots();
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> addFavoriteHikingSpot(int userId, int hikingSpotId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        HikingSpot hikingSpot = hikingSpotRepository.findById(hikingSpotId)
            .orElseThrow(() -> new RuntimeException("HikingSpot non trouvé"));
        
        if (user.getFavoriteHikingSpots().contains(hikingSpot)) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Ce hiking spot est déjà dans vos favoris")
                    .build());
        }
        
        user.getFavoriteHikingSpots().add(hikingSpot);
        userRepository.save(user);
        
        return ResponseEntity.ok()
            .body(AuthenticationResponseDto.builder()
                .message("Hiking spot ajouté aux favoris avec succès")
                .userApp(user)
                .build());
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> removeFavoriteHikingSpot(int userId, int hikingSpotId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        HikingSpot hikingSpot = hikingSpotRepository.findById(hikingSpotId)
            .orElseThrow(() -> new RuntimeException("HikingSpot non trouvé"));
        
        if (!user.getFavoriteHikingSpots().contains(hikingSpot)) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Ce hiking spot n'est pas dans vos favoris")
                    .build());
        }
        
        user.getFavoriteHikingSpots().remove(hikingSpot);
        userRepository.save(user);
        
        return ResponseEntity.ok()
            .body(AuthenticationResponseDto.builder()
                .message("Hiking spot retiré des favoris avec succès")
                .userApp(user)
                .build());
    }

    @Override
    public List<HikingSpot> getFavoriteHikingSpots(int userId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        return user.getFavoriteHikingSpots();
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> addFriend(int userId, int friendId) {
        if (userId == friendId) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Vous ne pouvez pas vous ajouter vous-même comme ami")
                    .build());
        }
        
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        UserApp friend = userRepository.findById(friendId)
            .orElseThrow(() -> new UsernameNotFoundException("Ami non trouvé"));
        
        if (user.getFriends().contains(friend)) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Cet utilisateur est déjà votre ami")
                    .build());
        }
        
        user.getFriends().add(friend);
        friend.getFriends().add(user);
        
        userRepository.save(user);
        userRepository.save(friend);
        
        return ResponseEntity.ok()
            .body(AuthenticationResponseDto.builder()
                .message("Ami ajouté avec succès")
                .userApp(user)
                .build());
    }

    @Override
    public ResponseEntity<AuthenticationResponseDto> removeFriend(int userId, int friendId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        UserApp friend = userRepository.findById(friendId)
            .orElseThrow(() -> new UsernameNotFoundException("Ami non trouvé"));
        
        if (!user.getFriends().contains(friend)) {
            return ResponseEntity.badRequest()
                .body(AuthenticationResponseDto.builder()
                    .message("Cet utilisateur n'est pas votre ami")
                    .build());
        }
        
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
        
        userRepository.save(user);
        userRepository.save(friend);
        
        return ResponseEntity.ok()
            .body(AuthenticationResponseDto.builder()
                .message("Ami retiré avec succès")
                .userApp(user)
                .build());
    }

    @Override
    public List<UserApp> getFriends(int userId) {
        UserApp user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        return user.getFriends();
    }
}