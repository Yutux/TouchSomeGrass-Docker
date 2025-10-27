package com.exemple.security.services;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.exemple.security.dtos.SpotDto;
import com.exemple.security.dtos.SpotRequestDto;
import com.exemple.security.dtos.SpotResponseDto;
import com.exemple.security.dtos.SpotsListResponseDto;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.tools.GoogleMapsUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SpotServiceConnector {

    @Autowired
    private SpotService spotService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SpotRepository spotRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${google.maps.api.key}")
    private String googleApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<SpotResponseDto> createSpot(SpotRequestDto spot, MultipartFile[] imageFiles) {

        // 1️⃣ Vérifier l'authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("=== 📝 CREATE SPOT ===");
        System.out.println("👤 Username: " + username);
        System.out.println("📍 Spot name: " + spot.getName());
        
        Optional<UserApp> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(
                SpotResponseDto.builder()
                    .message("❌ Utilisateur non trouvé")
                    .build()
            );
        }

        UserApp user = userOptional.get();
        List<String> allImagePaths = new ArrayList<>();

        // 2️⃣ Ajouter les photo_reference Google Maps
        if (spot.getImageUrls() != null && !spot.getImageUrls().isEmpty()) {
            System.out.println("🌐 Photo references Google reçues: " + spot.getImageUrls().size());
            
            for (String photoRef : spot.getImageUrls()) {
                if (photoRef != null && !photoRef.isEmpty()) {
                    if (GoogleMapsUtils.isGooglePhotoReference(photoRef)) {
                        allImagePaths.add(photoRef);
                        System.out.println("  ✅ Photo reference ajoutée: " + photoRef.substring(0, 30) + "...");
                    } else {
                        System.out.println("  ⚠️ Ignoré (pas une photo_reference): " + photoRef);
                    }
                }
            }
        }

        // 3️⃣ Ajouter les fichiers uploadés manuellement
        if (imageFiles != null && imageFiles.length > 0) {
            System.out.println("📁 Fichiers uploadés: " + imageFiles.length);
            
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String savedPath = fileStorageService.saveFile(file);
                        allImagePaths.add(savedPath);
                        System.out.println("  ✅ Fichier sauvegardé: " + savedPath);
                    } catch (Exception e) {
                        System.err.println("  ❌ Erreur sauvegarde fichier: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            SpotResponseDto.builder()
                                .message("❌ Erreur lors de l'enregistrement d'une image: " + e.getMessage())
                                .build()
                        );
                    }
                }
            }
        }

        // 4️⃣ Définir l'image principale
        String mainImage = allImagePaths.isEmpty() ? null : allImagePaths.get(0);
        
        System.out.println("📊 Total images: " + allImagePaths.size());
        System.out.println("🖼️ Image principale: " + (mainImage != null ? mainImage.substring(0, Math.min(30, mainImage.length())) + "..." : "aucune"));

        // 5️⃣ Créer le Spot
        Spot newSpot = Spot.builder()
            .name(spot.getName())
            .description(spot.getDescription())
            .latitude(spot.getLatitude())
            .longitude(spot.getLongitude())
            .imagePath(mainImage)
            .imageUrls(allImagePaths)
            .placeId(spot.getPlaceId())
            .lastPhotoRefresh(LocalDateTime.now()) // 🔥 AJOUT
            .creator(user)
            .build();

        // 6️⃣ Sauvegarder
        spotService.saveSpot(newSpot);
        user.getSpots().add(newSpot);
        userRepository.save(user);

        System.out.println("✅ Spot créé avec succès: ID=" + newSpot.getId());
        System.out.println("===================\n");

        return ResponseEntity.ok(
            SpotResponseDto.builder()
                .message("✅ Spot créé avec succès avec " + allImagePaths.size() + " image(s)")
                .newSpot(newSpot)
                .build()
        );
    }
    
    public ResponseEntity<SpotResponseDto> updateSpot(int id, SpotRequestDto req, MultipartFile[] imageFiles) {
        System.out.println("=== 🔄 UPDATE SPOT ===");
        System.out.println("📍 Spot ID: " + id);
        
        // 1️⃣ Authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(SpotResponseDto.builder()
                    .message("⛔ Vous devez être connecté pour modifier un spot.")
                    .build());
        }

        String username = authentication.getName();
        Optional<UserApp> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(SpotResponseDto.builder()
                    .message("❌ Utilisateur introuvable.")
                    .build());
        }

        UserApp user = userOptional.get();

        // 2️⃣ Rechercher le spot
        Optional<Spot> optionalSpot = spotRepository.findById(id);
        if (optionalSpot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SpotResponseDto.builder()
                    .message("❌ Spot non trouvé.")
                    .build());
        }

        Spot spot = optionalSpot.get();

        // 3️⃣ Vérifier les permissions
        boolean isCreator = spot.getCreator() != null &&
            spot.getCreator().getEmail().equalsIgnoreCase(user.getEmail());
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);

        if (!isCreator && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(SpotResponseDto.builder()
                    .message("🚫 Vous n'êtes pas autorisé à modifier ce spot.")
                    .build());
        }

        // 4️⃣ Mettre à jour les champs basiques
        spot.setName(req.getName() != null ? req.getName() : spot.getName());
        spot.setDescription(req.getDescription() != null ? req.getDescription() : spot.getDescription());
        spot.setLatitude(req.getLatitude());
        spot.setLongitude(req.getLongitude());

        // 5️⃣ Gestion des images avec refresh automatique
        System.out.println("🖼️ Gestion des images...");
        List<String> currentImages = spot.getImageUrls() != null ? new ArrayList<>(spot.getImageUrls()) : new ArrayList<>();
        List<String> updatedImages = new ArrayList<>();

        // Ajouter les images de la requête
        if (req.getImageUrls() != null) {
            for (String imageUrl : req.getImageUrls()) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    updatedImages.add(imageUrl);
                    if (GoogleMapsUtils.isGooglePhotoReference(imageUrl)) {
                        System.out.println("  🔄 Photo reference mise à jour");
                    }
                }
            }
        }

        // Si des photo_reference ont été mises à jour, actualiser lastPhotoRefresh
        boolean hasNewPhotoReferences = updatedImages.stream()
            .anyMatch(GoogleMapsUtils::isGooglePhotoReference);
        
        if (hasNewPhotoReferences) {
            spot.setLastPhotoRefresh(LocalDateTime.now());
            System.out.println("  ✅ Date de refresh mise à jour");
        }

        // Identifier les fichiers à supprimer (seulement les fichiers locaux)
        List<String> imagesToDelete = currentImages.stream()
            .filter(img -> !updatedImages.contains(img))
            .filter(img -> !GoogleMapsUtils.isGooglePhotoReference(img))
            .collect(Collectors.toList());

        for (String img : imagesToDelete) {
            try {
                fileStorageService.deleteFile(img);
                System.out.println("  🗑️ Fichier supprimé: " + img);
            } catch (Exception e) {
                System.err.println("  ⚠️ Erreur suppression fichier: " + img);
            }
        }

        // Ajouter les nouveaux fichiers uploadés
        if (imageFiles != null && imageFiles.length > 0) {
            System.out.println("  📁 Nouveaux fichiers: " + imageFiles.length);
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String newPath = fileStorageService.saveFile(file);
                        updatedImages.add(newPath);
                        System.out.println("    ✅ Fichier ajouté: " + newPath);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(SpotResponseDto.builder()
                                .message("❌ Erreur lors de l'upload d'une image: " + e.getMessage())
                                .build());
                    }
                }
            }
        }

        spot.setImageUrls(updatedImages);
        spot.setImagePath(updatedImages.isEmpty() ? null : updatedImages.get(0));

        spotRepository.save(spot);
        
        System.out.println("✅ Spot mis à jour avec succès");
        System.out.println("===================\n");

        return ResponseEntity.ok(
            SpotResponseDto.builder()
                .message("✅ Spot mis à jour avec succès.")
                .newSpot(spot)
                .build()
        );
    }

    public ResponseEntity<SpotResponseDto> getSpot(int id) {
        Optional<Spot> findOne = spotService.getSpotById(id);

        if (findOne.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                SpotResponseDto.builder()
                    .message("❌ Aucun Spot trouvé avec cet ID")
                    .newSpot(null)
                    .build()
            );
        }

        Spot spot = findOne.get();

        List<String> fullImageUrls = new ArrayList<>();
        if (spot.getImageUrls() != null) {
            for (String imagePath : spot.getImageUrls()) {
                fullImageUrls.add(imagePath);
            }
        }

        return ResponseEntity.ok(
            SpotResponseDto.builder()
                .message("✅ Spot récupéré avec succès")
                .newSpot(Spot.builder()
                    .id(spot.getId())
                    .name(spot.getName())
                    .description(spot.getDescription())
                    .latitude(spot.getLatitude())
                    .longitude(spot.getLongitude())
                    .imagePath(spot.getImagePath())
                    .imageUrls(fullImageUrls)
                    .creator(spot.getCreator())
                    .build())
                .creatorname(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                .build()
        );
    }
    
    public ResponseEntity<SpotsListResponseDto> getAllSpots() {
        List<Spot> spots = spotService.getAllSpots();

        if (spots.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<SpotDto> spotDtos = spots.stream()
            .map(spot -> {
                List<String> imageUrls = spot.getImageUrls() != null 
                    ? new ArrayList<>(spot.getImageUrls()) 
                    : new ArrayList<>();

                return SpotDto.builder()
                    .id(spot.getId())
                    .name(spot.getName())
                    .description(spot.getDescription())
                    .latitude(spot.getLatitude())
                    .longitude(spot.getLongitude())
                    .imagePath(spot.getImagePath())
                    .imageUrls(imageUrls)
                    .creator(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                    .build();
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(
            SpotsListResponseDto.builder()
                .message("✅ Liste des spots récupérée avec succès")
                .spots(spotDtos)
                .build()
        );
    }
    
    public ResponseEntity<SpotResponseDto> deleteSpot(int id) {
        System.out.println("=== 🗑️ DELETE SPOT ===");
        System.out.println("📍 Spot ID: " + id);
        
        // 1️⃣ Authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SpotResponseDto.builder()
                            .message("⛔ Vous devez être connecté pour supprimer un spot.")
                            .build());
        }

        String username = authentication.getName();
        Optional<UserApp> userOptional = userRepository.findByEmail(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SpotResponseDto.builder()
                            .message("❌ Utilisateur introuvable.")
                            .build());
        }

        UserApp user = userOptional.get();

        // 2️⃣ Recherche du spot
        Optional<Spot> optionalSpot = spotRepository.findById(id);
        if (optionalSpot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SpotResponseDto.builder()
                            .message("❌ Aucun spot trouvé avec cet ID.")
                            .build());
        }

        Spot spot = optionalSpot.get();

        // 3️⃣ Vérification des permissions
        boolean isCreator = spot.getCreator() != null &&
                spot.getCreator().getEmail().equalsIgnoreCase(user.getEmail());

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);

        if (!isCreator && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(SpotResponseDto.builder()
                            .message("🚫 Vous n'êtes pas autorisé à supprimer ce spot.")
                            .build());
        }

        // 4️⃣ Supprimer uniquement les fichiers locaux
        if (spot.getImageUrls() != null) {
            System.out.println("🗑️ Suppression des fichiers locaux...");
            for (String img : spot.getImageUrls()) {
                if (img != null && !GoogleMapsUtils.isGooglePhotoReference(img) && !img.startsWith("http")) {
                    try {
                        fileStorageService.deleteFile(img);
                        System.out.println("  ✅ Fichier supprimé: " + img);
                    } catch (Exception e) {
                        System.err.println("  ⚠️ Erreur suppression: " + img);
                    }
                }
            }
        }

        // 5️⃣ Suppression du spot
        spotRepository.deleteById(id);
        
        System.out.println("✅ Spot supprimé avec succès");
        System.out.println("===================\n");

        return ResponseEntity.ok(
                SpotResponseDto.builder()
                        .message("🗑️ Spot supprimé avec succès.")
                        .build()
        );
    }

}
