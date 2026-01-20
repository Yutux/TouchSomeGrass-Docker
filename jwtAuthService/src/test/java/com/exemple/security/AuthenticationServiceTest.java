package com.exemple.security;
import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.AuthenticationRequestDto;
import com.exemple.security.dtos.AuthenticationResponseDto;
import com.exemple.security.dtos.RegisterRequestDto;
import com.exemple.security.dtos.UserAppListDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Role;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.RoleRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.services.AccountService;
import com.exemple.security.services.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthenticationService
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserApp testUser;
    private Role userRole;
    private RegisterRequestDto registerRequest;
    private AuthenticationRequestDto authRequest;

    @BeforeEach
    void setUp() {
        // Créer un rôle de test
        userRole = Role.builder()
                .id(1)
                .roleName(RoleName.USER)
                .build();

        // Créer un utilisateur de test
        testUser = UserApp.builder()
                .id(1)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .roles(Arrays.asList(userRole))
                .hikingSpots(new ArrayList<>())
                .spots(new ArrayList<>())
                .build();

        // Créer une requête d'inscription
        registerRequest = RegisterRequestDto.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("Password123!")
                .roles(new ArrayList<>())
                .build();

        // Créer une requête d'authentification
        authRequest = AuthenticationRequestDto.builder()
                .email("john.doe@example.com")
                .password("Password123!")
                .build();
    }

    // ==================== TESTS REGISTER ====================

    @Test
    @DisplayName("Register - Nouvel utilisateur avec succès")
    void register_NewUser_Success() {
        // Given
        when(repository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.USER)).thenReturn(userRole);
        when(accountService.addNewUser(any(UserApp.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(UserApp.class))).thenReturn("generated.jwt.token");

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Enregistrement avec succes.");
        assertThat(response.getHeaders().getFirst("Authorization")).isEqualTo("Bearer generated.jwt.token");

        verify(repository).findByEmail(registerRequest.getEmail());
        verify(accountService).addNewUser(any(UserApp.class));
        verify(jwtService).generateToken(any(UserApp.class));
    }

    @Test
    @DisplayName("Register - Email déjà existant - Échec")
    void register_EmailAlreadyExists_ReturnsBadRequest() {
        // Given
        when(repository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Un utilisateur avec cet e-mail existe déjà");

        verify(repository).findByEmail(registerRequest.getEmail());
        verify(accountService, never()).addNewUser(any(UserApp.class));
        verify(jwtService, never()).generateToken(any(UserApp.class));
    }
    
    // ==================== TESTS AUTHENTICATE ====================

    @Test
    @DisplayName("Authenticate - Identifiants valides - Succès")
    void authenticate_ValidCredentials_Success() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(repository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("generated.jwt.token");

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.authenticate(authRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User registered with success");
        assertThat(response.getBody().getToken()).isEqualTo("generated.jwt.token");
        assertThat(response.getHeaders().getFirst("Authorization")).isEqualTo("Bearer generated.jwt.token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repository).findByEmail(authRequest.getEmail());
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Authenticate - Utilisateur non trouvé - Échec")
    void authenticate_UserNotFound_ThrowsException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(repository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repository).findByEmail(authRequest.getEmail());
        verify(jwtService, never()).generateToken(any(UserApp.class));
    }

    @Test
    @DisplayName("Authenticate - Mauvais mot de passe - Lève AuthenticationException")
    void authenticate_InvalidPassword_ThrowsAuthenticationException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                .isInstanceOf(org.springframework.security.core.AuthenticationException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(UserApp.class));
    }

    // ==================== TESTS GET ALL USERS ====================

    @Test
    @DisplayName("GetAllUsers - Retourne liste d'utilisateurs")
    void getAllUsers_ReturnsUserList() {
        // Given
        UserApp user2 = UserApp.builder()
                .id(2)
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@example.com")
                .build();

        List<UserApp> userList = Arrays.asList(testUser, user2);
        when(repository.findAll()).thenReturn(userList);

        // When
        ResponseEntity<UserAppListDto> response = authenticationService.getAllUsers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Look at them running around");
        assertThat(response.getBody().getUserList()).hasSize(2);
        assertThat(response.getBody().getUserList()).containsExactly(testUser, user2);

        verify(repository).findAll();
    }

    @Test
    @DisplayName("GetAllUsers - Liste vide")
    void getAllUsers_EmptyList_ReturnsEmptyList() {
        // Given
        when(repository.findAll()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<UserAppListDto> response = authenticationService.getAllUsers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserList()).isEmpty();
    }

    // ==================== TESTS GET USER BY ID ====================

    @Test
    @DisplayName("GetUserById - Utilisateur trouvé - Succès")
    void getUserById_UserExists_Success() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.getUserById(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User found here he is");
        assertThat(response.getBody().getUserApp()).isEqualTo(testUser);

        verify(repository).findById(1);
    }

    @Test
    @DisplayName("GetUserById - Utilisateur non trouvé - Échec")
    void getUserById_UserNotFound_ThrowsException() {
        // Given
        when(repository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.getUserById(999))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User Not found");

        verify(repository).findById(999);
    }

    // ==================== TESTS DELETE ====================

    @Test
    @DisplayName("Delete - Suppression d'utilisateur - Succès")
    void delete_UserExists_Success() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.of(testUser));
        doNothing().when(repository).delete(testUser);

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.delete(1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Good he is gone");

        verify(repository).findById(1);
        verify(repository).delete(testUser);
    }

    @Test
    @DisplayName("Delete - Utilisateur non trouvé - Échec")
    void delete_UserNotFound_ThrowsException() {
        // Given
        when(repository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.delete(999))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User Not found");

        verify(repository).findById(999);
        verify(repository, never()).delete(any(UserApp.class));
    }

    // ==================== TESTS IS LOGGIN ====================

    @Test
    @DisplayName("IsLoggin - Token valide avec spots - Succès")
    void isLoggin_ValidTokenWithSpots_Success() {
        // Given
        String token = "Bearer valid.jwt.token";
        HikingSpot hikingSpot = HikingSpot.builder()
                .id(1)
                .name("Mountain Trail")
                .build();
        Spot spot = Spot.builder()
                .id(1)
                .name("Park Bench")
                .build();

        testUser.setHikingSpots(Arrays.asList(hikingSpot));
        testUser.setSpots(Arrays.asList(spot));

        when(jwtService.extractUsername(token)).thenReturn("john.doe@example.com");
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.isLoggin(token);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("success");
        assertThat(response.getBody().getUserApp()).isEqualTo(testUser);
        assertThat(response.getBody().getUserHikingSpots()).hasSize(1);
        assertThat(response.getBody().getUserSpots()).hasSize(1);

        verify(jwtService).extractUsername(token);
        verify(repository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("IsLoggin - Token null - Retourne UNAUTHORIZED")
    void isLoggin_NullToken_ReturnsUnauthorized() {
        // Given
        when(jwtService.extractUsername(null)).thenReturn(null);

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.isLoggin(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid token");
    }

    @Test
    @DisplayName("IsLoggin - Token vide - Retourne UNAUTHORIZED")
    void isLoggin_EmptyToken_ReturnsUnauthorized() {
        // Given
        when(jwtService.extractUsername("")).thenReturn("");

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.isLoggin("");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid token");
    }

    @Test
    @DisplayName("IsLoggin - Utilisateur non trouvé - Retourne INTERNAL_SERVER_ERROR")
    void isLoggin_UserNotFound_ReturnsInternalServerError() {
        // Given
        String token = "Bearer valid.jwt.token";
        when(jwtService.extractUsername(token)).thenReturn("nonexistent@example.com");
        when(repository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.isLoggin(token);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Error processing request");
    }

    @Test
    @DisplayName("IsLoggin - Exception lors extraction token - Retourne INTERNAL_SERVER_ERROR")
    void isLoggin_ExceptionDuringExtraction_ReturnsInternalServerError() {
        // Given
        String token = "Bearer invalid.token";
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid JWT signature"));

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.isLoggin(token);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Error processing request");
        assertThat(response.getBody().getMessage()).contains("Invalid JWT signature");
    }

    @Test
    @DisplayName("IsLoggin - Utilisateur sans spots - Succès")
    void isLoggin_UserWithoutSpots_Success() {
        // Given
        String token = "Bearer valid.jwt.token";
        testUser.setHikingSpots(new ArrayList<>());
        testUser.setSpots(new ArrayList<>());

        when(jwtService.extractUsername(token)).thenReturn("john.doe@example.com");
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<AuthenticationResponseDto> response = authenticationService.isLoggin(token);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUserHikingSpots()).isEmpty();
        assertThat(response.getBody().getUserSpots()).isEmpty();
    }
}