package com.exemple.security;

import com.exemple.security.dtos.SpotRequestDto;
import com.exemple.security.dtos.SpotResponseDto;
import com.exemple.security.dtos.SpotsListResponseDto;
import com.exemple.security.entities.Role;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.services.FileStorageService;
import com.exemple.security.services.SpotService;
import com.exemple.security.services.SpotServiceConnector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SpotServiceConnector
 */
@ExtendWith(MockitoExtension.class)
class SpotServiceConnectorTest {

    @Mock
    private SpotService spotService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SpotServiceConnector spotServiceConnector;

    private UserApp testUser;
    private UserApp adminUser;
    private Spot testSpot;
    private SpotRequestDto requestDto;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Créer les rôles
        userRole = Role.builder()
                .id(1)
                .roleName(RoleName.USER)
                .build();

        adminRole = Role.builder()
                .id(2)
                .roleName(RoleName.ADMIN)
                .build();

        // Créer un utilisateur normal
        testUser = UserApp.builder()
                .id(1)
                .email("test@example.com")
                .firstname("Test")
                .lastname("User")
                .roles(Arrays.asList(userRole))
                .spots(new ArrayList<>())
                .build();

        // Créer un admin
        adminUser = UserApp.builder()
                .id(2)
                .email("admin@example.com")
                .firstname("Admin")
                .lastname("User")
                .roles(Arrays.asList(adminRole))
                .build();

        // Créer un Spot de test
        testSpot = Spot.builder()
                .id(1)
                .name("Test Spot")
                .description("Test description")
                .latitude(48.8566)
                .longitude(2.3522)
                .imagePath("/uploads/test.jpg")
                .imageUrls(Arrays.asList("/uploads/test.jpg"))
                .placeId("ChIJD7fiBh9u5kcRYJSMaMOCCwQ")
                .lastPhotoRefresh(LocalDateTime.now())
                .creator(testUser)
                .build();

        // Créer une requête de test
        requestDto = SpotRequestDto.builder()
                .name("New Spot")
                .description("New description")
                .latitude(48.8606)
                .longitude(2.3376)
                .imageUrls(new ArrayList<>())
                .placeId("ChIJTest123")
                .build();

        // Configuration du contexte de sécurité
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== TESTS CREATE SPOT ====================

    @Test
    @DisplayName("CreateSpot - Utilisateur authentifié - Succès")
    void createSpot_AuthenticatedUser_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotService.saveSpot(any(Spot.class))).thenReturn(testSpot);
        when(userRepository.save(any(UserApp.class))).thenReturn(testUser);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.createSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("créé avec succès");
        verify(spotService).saveSpot(any(Spot.class));
        verify(userRepository).save(any(UserApp.class));
    }

    @Test
    @DisplayName("CreateSpot - Utilisateur non trouvé - Échec")
    void createSpot_UserNotFound_ReturnsBadRequest() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.createSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("Utilisateur non trouvé");
        verify(spotService, never()).saveSpot(any(Spot.class));
    }

    @Test
    @DisplayName("CreateSpot - Avec fichiers uploadés - Succès")
    void createSpot_WithUploadedFiles_Success() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "spot1.jpg", "image/jpeg", "test image".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "spot2.jpg", "image/jpeg", "test image 2".getBytes()
        );
        MultipartFile[] files = {file1, file2};

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.saveFile(any(MultipartFile.class)))
            .thenReturn("/uploads/spot1.jpg")
            .thenReturn("/uploads/spot2.jpg");
        when(spotService.saveSpot(any(Spot.class))).thenReturn(testSpot);
        when(userRepository.save(any(UserApp.class))).thenReturn(testUser);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.createSpot(requestDto, files);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("2 image(s)");
        verify(fileStorageService, times(2)).saveFile(any(MultipartFile.class));
    }

    @Test
    @DisplayName("CreateSpot - Avec photo references Google Maps - Succès")
    void createSpot_WithGooglePhotoReferences_Success() {
        // Given
        List<String> photoRefs = Arrays.asList(
            "AeJYXqDP1YxJkKvxvMyJtRZpTHIqYQ7Z8bCxYRmp3oNxJW9", // Photo reference (>30 chars)
            "CmRaAAAAnEwMxMjIwNzA5MjEwMTU0NzQ2MjY4NTI2Nzg5"      // Photo reference (>30 chars)
        );
        requestDto.setImageUrls(photoRefs);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotService.saveSpot(any(Spot.class))).thenReturn(testSpot);
        when(userRepository.save(any(UserApp.class))).thenReturn(testUser);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.createSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(spotService).saveSpot(any(Spot.class));
    }

    @Test
    @DisplayName("CreateSpot - Erreur upload fichier - Retourne erreur")
    void createSpot_FileUploadError_ReturnsError() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "error.jpg", "image/jpeg", "test".getBytes()
        );
        MultipartFile[] files = {file};

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.saveFile(any(MultipartFile.class)))
            .thenThrow(new RuntimeException("Storage error"));

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.createSpot(requestDto, files);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("Erreur");
        verify(spotService, never()).saveSpot(any(Spot.class));
    }

    @Test
    @DisplayName("CreateSpot - Sans images - Succès")
    void createSpot_NoImages_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotService.saveSpot(any(Spot.class))).thenReturn(testSpot);
        when(userRepository.save(any(UserApp.class))).thenReturn(testUser);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.createSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("0 image(s)");
    }

    // ==================== TESTS UPDATE SPOT ====================

    @Test
    @DisplayName("UpdateSpot - Créateur modifie son spot - Succès")
    void updateSpot_CreatorUpdatesOwnSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));
        when(spotRepository.save(any(Spot.class))).thenReturn(testSpot);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.updateSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("mis à jour avec succès");
        verify(spotRepository).save(any(Spot.class));
    }

    @Test
    @DisplayName("UpdateSpot - Admin modifie un spot - Succès")
    void updateSpot_AdminUpdatesSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));
        when(spotRepository.save(any(Spot.class))).thenReturn(testSpot);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.updateSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(spotRepository).save(any(Spot.class));
    }

    @Test
    @DisplayName("UpdateSpot - Non authentifié - Échec")
    void updateSpot_NotAuthenticated_ReturnsUnauthorized() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.updateSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).contains("connecté");
    }

    @Test
    @DisplayName("UpdateSpot - Spot non trouvé - Échec")
    void updateSpot_SpotNotFound_ReturnsNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.updateSpot(999, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("non trouvé");
    }

    @Test
    @DisplayName("UpdateSpot - Utilisateur non autorisé - Échec")
    void updateSpot_UnauthorizedUser_ReturnsForbidden() {
        // Given
        UserApp otherUser = UserApp.builder()
                .id(3)
                .email("other@example.com")
                .roles(Arrays.asList(userRole))
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(otherUser);
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.updateSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).contains("pas autorisé");
        verify(spotRepository, never()).save(any(Spot.class));
    }

    @Test
    @DisplayName("UpdateSpot - Avec nouvelles images - Met à jour lastPhotoRefresh")
    void updateSpot_WithNewPhotoReferences_UpdatesLastPhotoRefresh() {
        // Given
        List<String> newPhotoRefs = Arrays.asList(
            "AeJYXqDP1YxJkKvxvMyJtRZpTHIqYQ7Z8bCxYRmp3oNxJW9"
        );
        requestDto.setImageUrls(newPhotoRefs);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));
        when(spotRepository.save(any(Spot.class))).thenReturn(testSpot);

        // When
        ResponseEntity<SpotResponseDto> response = 
            spotServiceConnector.updateSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(spotRepository).save(any(Spot.class));
    }

    // ==================== TESTS GET SPOT ====================

    @Test
    @DisplayName("GetSpot - ID valide - Succès")
    void getSpot_ValidId_Success() {
        // Given
        when(spotService.getSpotById(1)).thenReturn(Optional.of(testSpot));

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.getSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNewSpot()).isNotNull();
        assertThat(response.getBody().getNewSpot().getName()).isEqualTo("Test Spot");
        assertThat(response.getBody().getCreatorname()).isEqualTo("User");
    }

    @Test
    @DisplayName("GetSpot - ID inexistant - Retourne 404")
    void getSpot_InvalidId_ReturnsNotFound() {
        // Given
        when(spotService.getSpotById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.getSpot(999);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("Aucun Spot trouvé");
    }

    // ==================== TESTS GET ALL SPOTS ====================

    @Test
    @DisplayName("GetAllSpots - Liste non vide - Succès")
    void getAllSpots_NonEmptyList_Success() {
        // Given
        List<Spot> spots = Arrays.asList(testSpot);
        when(spotService.getAllSpots()).thenReturn(spots);

        // When
        ResponseEntity<SpotsListResponseDto> response = spotServiceConnector.getAllSpots();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSpots()).hasSize(1);
        assertThat(response.getBody().getMessage()).contains("récupérée avec succès");
    }

    @Test
    @DisplayName("GetAllSpots - Liste vide - Retourne 204")
    void getAllSpots_EmptyList_ReturnsNoContent() {
        // Given
        when(spotService.getAllSpots()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<SpotsListResponseDto> response = spotServiceConnector.getAllSpots();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GetAllSpots - Vérifie les URLs d'images")
    void getAllSpots_ChecksImageUrls_Success() {
        // Given
        List<Spot> spots = Arrays.asList(testSpot);
        when(spotService.getAllSpots()).thenReturn(spots);

        // When
        ResponseEntity<SpotsListResponseDto> response = spotServiceConnector.getAllSpots();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSpots().get(0).getImageUrls()).isNotEmpty();
    }

    // ==================== TESTS DELETE SPOT ====================

    @Test
    @DisplayName("DeleteSpot - Créateur supprime son spot - Succès")
    void deleteSpot_CreatorDeletesOwnSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));
        doNothing().when(spotRepository).deleteById(1);

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.deleteSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("supprimé avec succès");
        verify(spotRepository).deleteById(1);
    }

    @Test
    @DisplayName("DeleteSpot - Admin supprime un spot - Succès")
    void deleteSpot_AdminDeletesSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));
        doNothing().when(spotRepository).deleteById(1);

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.deleteSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(spotRepository).deleteById(1);
    }

    @Test
    @DisplayName("DeleteSpot - Non authentifié - Échec")
    void deleteSpot_NotAuthenticated_ReturnsUnauthorized() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.deleteSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(spotRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("DeleteSpot - Spot non trouvé - Échec")
    void deleteSpot_SpotNotFound_ReturnsNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.deleteSpot(999);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(spotRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("DeleteSpot - Utilisateur non autorisé - Échec")
    void deleteSpot_UnauthorizedUser_ReturnsForbidden() {
        // Given
        UserApp otherUser = UserApp.builder()
                .id(3)
                .email("other@example.com")
                .roles(Arrays.asList(userRole))
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(otherUser);
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(testSpot));

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.deleteSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(spotRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("DeleteSpot - Supprime les fichiers locaux uniquement")
    void deleteSpot_DeletesLocalFilesOnly_Success() throws Exception {
        // Given
        Spot spotWithMixedImages = Spot.builder()
                .id(1)
                .name("Mixed Images Spot")
                .imageUrls(Arrays.asList(
                    "/uploads/local.jpg",  // Fichier local
                    "AeJYXqDP1YxJkKvxvMyJtRZpTHIqYQ7Z8bCxYRmp3oNxJW9", // Photo reference
                    "http://example.com/remote.jpg"  // URL externe
                ))
                .creator(testUser)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(spotRepository.findById(1)).thenReturn(Optional.of(spotWithMixedImages));
        doNothing().when(fileStorageService).deleteFile(anyString());
        doNothing().when(spotRepository).deleteById(1);

        // When
        ResponseEntity<SpotResponseDto> response = spotServiceConnector.deleteSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(fileStorageService, times(1)).deleteFile("/uploads/local.jpg");
        verify(spotRepository).deleteById(1);
    }
}