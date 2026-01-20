package com.exemple.security;

import com.exemple.security.dtos.HikingSpotListResponseDto;
import com.exemple.security.dtos.HikingSpotRequestDto;
import com.exemple.security.dtos.HikingSpotResponseDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Role;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.HikingSpotRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.services.FileStorageService;
import com.exemple.security.services.HikingSpotService;
import com.exemple.security.services.HikingSpotServiceConnector;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour HikingSpotServiceConnector
 */
@ExtendWith(MockitoExtension.class)
class HikingSpotServiceConnectorTest {

    @Mock
    private HikingSpotRepository hikingSpotRepository;

    @Mock
    private HikingSpotService hikingSpotService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HikingSpotServiceConnector hikingSpotServiceConnector;

    private UserApp testUser;
    private UserApp adminUser;
    private HikingSpot testHikingSpot;
    private HikingSpotRequestDto requestDto;
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
                .build();

        // Créer un admin
        adminUser = UserApp.builder()
                .id(2)
                .email("admin@example.com")
                .firstname("Admin")
                .lastname("User")
                .roles(Arrays.asList(adminRole))
                .build();

        // Créer un HikingSpot de test
        testHikingSpot = HikingSpot.builder()
                .id(1)
                .name("Test Trail")
                .description("Test description")
                .region("Test Region")
                .distance(10.5)
                .duration(120)
                .startLatitude(48.8566)
                .startLongitude(2.3522)
                .endLatitude(48.8606)
                .endLongitude(2.3376)
                .imagePath("/uploads/test.jpg")
                .imageUrls(Arrays.asList("/uploads/test.jpg"))
                .creator(testUser)
                .waypoints(new ArrayList<>())
                .build();

        // Créer une requête de test
        requestDto = HikingSpotRequestDto.builder()
                .name("New Trail")
                .description("New description")
                .region("Ile-de-France")
                .distance(15.0)
                .duration(180)
                .travelMode("WALKING")
                .startLatitude(48.8566)
                .startLongitude(2.3522)
                .endLatitude(48.8606)
                .endLongitude(2.3376)
                .imageUrls(new ArrayList<>())
                .waypoints(new ArrayList<>())
                .build();

        // Configuration du contexte de sécurité
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== TESTS CREATE HIKING SPOT ====================

    @Test
    @DisplayName("CreateHikingSpot - Utilisateur authentifié - Succès")
    void createHikingSpot_AuthenticatedUser_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hikingSpotRepository.save(any(HikingSpot.class))).thenReturn(testHikingSpot);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.createHikingSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("créé avec succès");
        verify(hikingSpotRepository).save(any(HikingSpot.class));
    }

    @Test
    @DisplayName("CreateHikingSpot - Utilisateur non trouvé - Échec")
    void createHikingSpot_UserNotFound_ReturnsBadRequest() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.createHikingSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("Utilisateur non trouvé");
        verify(hikingSpotRepository, never()).save(any(HikingSpot.class));
    }

    @Test
    @DisplayName("CreateHikingSpot - Avec fichiers uploadés - Succès")
    void createHikingSpot_WithFiles_Success() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "test1.jpg", "image/jpeg", "test image content".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "test2.jpg", "image/jpeg", "test image content 2".getBytes()
        );
        MultipartFile[] files = {file1, file2};

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.saveFile(any(MultipartFile.class)))
            .thenReturn("/uploads/test1.jpg")
            .thenReturn("/uploads/test2.jpg");
        when(hikingSpotRepository.save(any(HikingSpot.class))).thenReturn(testHikingSpot);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.createHikingSpot(requestDto, files);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(fileStorageService, times(2)).saveFile(any(MultipartFile.class));
        verify(hikingSpotRepository).save(any(HikingSpot.class));
    }

    @Test
    @DisplayName("CreateHikingSpot - Avec photo references Google - Succès")
    void createHikingSpot_WithGooglePhotoReferences_Success() {
        // Given
        List<String> photoRefs = Arrays.asList(
            "AeJYXqDP1YxJkKvxvMyJtRZpTHIqYQ7Z8bCxYRmp3oNxJW9", // Photo reference Google (plus de 30 chars)
            "CmRaAAAAnEwMxMjIwNzA5MjEwMTU0NzQ2MjY4NTI2Nzg5"      // Autre photo reference (plus de 30 chars)
        );
        requestDto.setImageUrls(photoRefs);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hikingSpotRepository.save(any(HikingSpot.class))).thenReturn(testHikingSpot);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.createHikingSpot(requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(hikingSpotRepository).save(any(HikingSpot.class));
    }


   

    @Test
    @DisplayName("CreateHikingSpot - Erreur upload fichier - Retourne erreur")
    void createHikingSpot_FileUploadError_ReturnsError() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test".getBytes()
        );
        MultipartFile[] files = {file};

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.saveFile(any(MultipartFile.class)))
            .thenThrow(new RuntimeException("Storage error"));

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.createHikingSpot(requestDto, files);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("Erreur");
        verify(hikingSpotRepository, never()).save(any(HikingSpot.class));
    }

    // ==================== TESTS UPDATE HIKING SPOT ====================

    @Test
    @DisplayName("UpdateHikingSpot - Créateur modifie son spot - Succès")
    void updateHikingSpot_CreatorUpdatesOwnSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hikingSpotRepository.findById(1)).thenReturn(Optional.of(testHikingSpot));
        when(hikingSpotRepository.save(any(HikingSpot.class))).thenReturn(testHikingSpot);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.updateHikingSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("mis à jour avec succès");
        verify(hikingSpotRepository).save(any(HikingSpot.class));
    }

    @Test
    @DisplayName("UpdateHikingSpot - Admin modifie un spot - Succès")
    void updateHikingSpot_AdminUpdatesSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(hikingSpotRepository.findById(1)).thenReturn(Optional.of(testHikingSpot));
        when(hikingSpotRepository.save(any(HikingSpot.class))).thenReturn(testHikingSpot);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.updateHikingSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(hikingSpotRepository).save(any(HikingSpot.class));
    }

    @Test
    @DisplayName("UpdateHikingSpot - Non authentifié - Échec")
    void updateHikingSpot_NotAuthenticated_ReturnsUnauthorized() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.updateHikingSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).contains("connecté");
    }

    @Test
    @DisplayName("UpdateHikingSpot - Spot non trouvé - Échec")
    void updateHikingSpot_SpotNotFound_ReturnsNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hikingSpotRepository.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.updateHikingSpot(999, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("non trouvé");
    }

    @Test
    @DisplayName("UpdateHikingSpot - Utilisateur non autorisé - Échec")
    void updateHikingSpot_UnauthorizedUser_ReturnsForbidden() {
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
        when(hikingSpotRepository.findById(1)).thenReturn(Optional.of(testHikingSpot));

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.updateHikingSpot(1, requestDto, new MultipartFile[0]);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).contains("pas autorisé");
        verify(hikingSpotRepository, never()).save(any(HikingSpot.class));
    }

    // ==================== TESTS GET HIKING SPOT ====================

    @Test
    @DisplayName("GetHikingSpot - ID valide - Succès")
    void getHikingSpot_ValidId_Success() {
        // Given
        when(hikingSpotService.getHikingSpotById(1)).thenReturn(Optional.of(testHikingSpot));

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.getHikingSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNewHikingSpot()).isNotNull();
        assertThat(response.getBody().getNewHikingSpot().getName()).isEqualTo("Test Trail");
    }

    @Test
    @DisplayName("GetHikingSpot - ID inexistant - Retourne 404")
    void getHikingSpot_InvalidId_ReturnsNotFound() {
        // Given
        when(hikingSpotService.getHikingSpotById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.getHikingSpot(999);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("Aucun HikingSpot trouvé");
    }

    // ==================== TESTS GET ALL HIKING SPOTS ====================

    @Test
    @DisplayName("GetAllHikingSpots - Liste non vide - Succès")
    void getAllHikingSpots_NonEmptyList_Success() {
        // Given
        List<HikingSpot> spots = Arrays.asList(testHikingSpot);
        when(hikingSpotService.getAllHikingSpots()).thenReturn(spots);

        // When
        ResponseEntity<HikingSpotListResponseDto> response = 
            hikingSpotServiceConnector.getAllHikingSpots();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHikingSpots()).hasSize(1);
        assertThat(response.getBody().getMessage()).contains("récupérée avec succès");
    }

    @Test
    @DisplayName("GetAllHikingSpots - Liste vide - Retourne 204")
    void getAllHikingSpots_EmptyList_ReturnsNoContent() {
        // Given
        when(hikingSpotService.getAllHikingSpots()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<HikingSpotListResponseDto> response = 
            hikingSpotServiceConnector.getAllHikingSpots();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ==================== TESTS DELETE HIKING SPOT ====================

    @Test
    @DisplayName("DeleteHikingSpot - Créateur supprime son spot - Succès")
    void deleteHikingSpot_CreatorDeletesOwnSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hikingSpotRepository.findById(1)).thenReturn(Optional.of(testHikingSpot));
        doNothing().when(hikingSpotRepository).deleteById(1);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.deleteHikingSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("supprimé avec succès");
        verify(hikingSpotRepository).deleteById(1);
    }

    @Test
    @DisplayName("DeleteHikingSpot - Admin supprime un spot - Succès")
    void deleteHikingSpot_AdminDeletesSpot_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(hikingSpotRepository.findById(1)).thenReturn(Optional.of(testHikingSpot));
        doNothing().when(hikingSpotRepository).deleteById(1);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.deleteHikingSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(hikingSpotRepository).deleteById(1);
    }

    @Test
    @DisplayName("DeleteHikingSpot - Non authentifié - Échec")
    void deleteHikingSpot_NotAuthenticated_ReturnsUnauthorized() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.deleteHikingSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(hikingSpotRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("DeleteHikingSpot - Spot non trouvé - Échec")
    void deleteHikingSpot_SpotNotFound_ReturnsNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hikingSpotRepository.findById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.deleteHikingSpot(999);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(hikingSpotRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("DeleteHikingSpot - Utilisateur non autorisé - Échec")
    void deleteHikingSpot_UnauthorizedUser_ReturnsForbidden() {
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
        when(hikingSpotRepository.findById(1)).thenReturn(Optional.of(testHikingSpot));

        // When
        ResponseEntity<HikingSpotResponseDto> response = 
            hikingSpotServiceConnector.deleteHikingSpot(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(hikingSpotRepository, never()).deleteById(anyInt());
    }
}
