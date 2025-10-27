package com.exemple.security.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.exemple.security.dtos.HikingSpotDto;
import com.exemple.security.dtos.HikingSpotListResponseDto;
import com.exemple.security.dtos.HikingSpotRequestDto;
import com.exemple.security.dtos.HikingSpotResponseDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.HikingWaypoint;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.HikingSpotRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.tools.GoogleMapsUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class HikingSpotServiceConnector {

    @Autowired
    private HikingSpotRepository hikingSpotRepository;
    
    @Autowired
    private HikingSpotService hikingSpotService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;

    public ResponseEntity<HikingSpotResponseDto> createHikingSpot(HikingSpotRequestDto req, MultipartFile[] files) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("=== 🏞️ CREATE HIKING SPOT ===");
        System.out.println("👤 Username: " + username);
        System.out.println("📍 Hiking Spot name: " + req.getName());
        
        Optional<UserApp> userOptional = userRepository.findByEmail(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(
                HikingSpotResponseDto.builder()
                    .message("❌ Utilisateur non trouvé")
                    .build()
            );
        }

        UserApp user = userOptional.get();
        List<String> allImagePaths = new ArrayList<>();

        // 1️⃣ Ajouter les photo_reference Google Maps
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            System.out.println("🌐 Photo references Google reçues: " + req.getImageUrls().size());
            
            for (String photoRef : req.getImageUrls()) {
                if (photoRef != null && !photoRef.isEmpty()) {
                    // Vérifier si c'est une photo_reference
                    if (GoogleMapsUtils.isGooglePhotoReference(photoRef)) {
                        allImagePaths.add(photoRef);
                        System.out.println("  ✅ Photo reference ajoutée: " + photoRef.substring(0, 30) + "...");
                    } else {
                        allImagePaths.add(photoRef); // Peut-être une URL ou autre
                        System.out.println("  ℹ️ Autre type d'image: " + photoRef.substring(0, 30) + "...");
                    }
                }
            }
        }

        // 2️⃣ Sauvegarder les fichiers uploadés
        if (files != null && files.length > 0) {
            System.out.println("📁 Fichiers uploadés: " + files.length);
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String savedPath = fileStorageService.saveFile(file);
                        allImagePaths.add(savedPath);
                        System.out.println("  ✅ Fichier sauvegardé: " + savedPath);
                    } catch (Exception e) {
                        System.err.println("  ❌ Erreur sauvegarde fichier: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(HikingSpotResponseDto.builder()
                                        .message("❌ Erreur lors de l'enregistrement d'une image : " + e.getMessage())
                                        .build());
                    }
                }
            }
        }

        // 3️⃣ Définir l'image principale
        String mainImage = !allImagePaths.isEmpty() ? allImagePaths.get(0) : null;
        
        System.out.println("📊 Total images: " + allImagePaths.size());
        System.out.println("🖼️ Image principale: " + (mainImage != null ? mainImage.substring(0, Math.min(30, mainImage.length())) + "..." : "aucune"));

        // 4️⃣ Construire le HikingSpot
        HikingSpot newHikingSpot = HikingSpot.builder()
                .name(req.getName())
                .description(req.getDescription())
                .region(req.getRegion())
                .distance(req.getDistance())
                .duration(req.getDuration())
                .travelMode(req.getTravelMode())
                .difficultyLevel(req.getDifficultyLevel())
                .startLatitude(req.getStartLatitude())
                .startLongitude(req.getStartLongitude())
                .endLatitude(req.getEndLatitude())
                .endLongitude(req.getEndLongitude())
                .imagePath(mainImage)
                .imageUrls(allImagePaths)
                //.placeId(req.getPlaceId()) // 🔥 AJOUT pour le refresh
                //.lastPhotoRefresh(LocalDateTime.now()) // 🔥 AJOUT
                .creator(user)
                .build();

        // 5️⃣ Ajouter les waypoints (escales)
        if (req.getWaypoints() != null && !req.getWaypoints().isEmpty()) {
            System.out.println("🗺️ Waypoints: " + req.getWaypoints().size());
            
            List<HikingWaypoint> waypointEntities = req.getWaypoints().stream().map(dto ->
                HikingWaypoint.builder()
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .placeId(dto.getPlaceId())
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .rating(dto.getRating())
                    .photos(dto.getPhotos()) // 🔥 Contient les photo_reference
                    .hikingSpot(newHikingSpot)
                    .build()
            ).collect(Collectors.toList());

            newHikingSpot.setWaypoints(waypointEntities);
        }

        // 6️⃣ Sauvegarde en base
        hikingSpotRepository.save(newHikingSpot);
        
        System.out.println("✅ HikingSpot créé avec succès: ID=" + newHikingSpot.getId());
        System.out.println("================================\n");

        return ResponseEntity.ok(
            HikingSpotResponseDto.builder()
                .message("🏞️ Hiking Spot créé avec succès avec " 
                    + (req.getWaypoints() != null ? req.getWaypoints().size() : 0)
                    + " escales et " + allImagePaths.size() + " images.")
                .build()
        );
    }

    public ResponseEntity<HikingSpotResponseDto> updateHikingSpot(int id, HikingSpotRequestDto req, MultipartFile[] files) {
        // 1️⃣ Authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HikingSpotResponseDto.builder()
                            .message("⛔ Vous devez être connecté pour modifier un spot.")
                            .build());
        }

        String username = authentication.getName();
        Optional<UserApp> userOptional = userRepository.findByEmail(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HikingSpotResponseDto.builder()
                            .message("❌ Utilisateur introuvable.")
                            .build());
        }

        UserApp user = userOptional.get();

        // 2️⃣ Recherche du spot
        Optional<HikingSpot> optionalHikingSpot = hikingSpotRepository.findById(id);

        if (optionalHikingSpot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(HikingSpotResponseDto.builder()
                            .message("❌ HikingSpot non trouvé.")
                            .build());
        }

        HikingSpot hikingSpot = optionalHikingSpot.get();

        // 3️⃣ Vérification des permissions
        boolean isCreator = hikingSpot.getCreator() != null &&
                hikingSpot.getCreator().getEmail().equalsIgnoreCase(user.getEmail());

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);

        if (!isCreator && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HikingSpotResponseDto.builder()
                            .message("🚫 Vous n'êtes pas autorisé à modifier ce spot.")
                            .build());
        }

        // 4️⃣ Mise à jour des champs simples
        hikingSpot.setName(req.getName() != null ? req.getName() : hikingSpot.getName());
        hikingSpot.setDescription(req.getDescription() != null ? req.getDescription() : hikingSpot.getDescription());
        hikingSpot.setRegion(req.getRegion() != null ? req.getRegion() : hikingSpot.getRegion());
        hikingSpot.setDistance(req.getDistance() > 0 ? req.getDistance() : hikingSpot.getDistance());
        hikingSpot.setDuration(req.getDuration() > 0 ? req.getDuration() : hikingSpot.getDuration());
        hikingSpot.setTravelMode(req.getTravelMode() != null ? req.getTravelMode() : hikingSpot.getTravelMode());
        hikingSpot.setStartLatitude(req.getStartLatitude());
        hikingSpot.setStartLongitude(req.getStartLongitude());
        hikingSpot.setEndLatitude(req.getEndLatitude());
        hikingSpot.setEndLongitude(req.getEndLongitude());

        // 5️⃣ Gestion des images
        List<String> currentImages = hikingSpot.getImageUrls() != null
                ? new ArrayList<>(hikingSpot.getImageUrls())
                : new ArrayList<>();

        List<String> updatedImages = new ArrayList<>();

        // Ajouter les images de la requête (photo_reference + anciens fichiers)
        if (req.getImageUrls() != null) {
            updatedImages.addAll(req.getImageUrls());
        }

        // Identifier les fichiers à supprimer (seulement les fichiers locaux)
        List<String> imagesToDelete = currentImages.stream()
                .filter(img -> !updatedImages.contains(img))
                .filter(img -> !GoogleMapsUtils.isGooglePhotoReference(img)) // Ne pas supprimer les photo_reference
                .collect(Collectors.toList());

        for (String img : imagesToDelete) {
            try {
                fileStorageService.deleteFile(img);
                System.out.println("🗑️ Fichier supprimé: " + img);
            } catch (Exception e) {
                System.err.println("⚠️ Erreur suppression fichier: " + img);
            }
        }

        // Ajouter les nouvelles images uploadées
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String savedPath = fileStorageService.saveFile(file);
                        updatedImages.add(savedPath);
                        System.out.println("✅ Nouveau fichier ajouté: " + savedPath);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(HikingSpotResponseDto.builder()
                                        .message("❌ Erreur lors de l'upload d'une image : " + e.getMessage())
                                        .build());
                    }
                }
            }
        }

        hikingSpot.setImageUrls(updatedImages);
        hikingSpot.setImagePath(updatedImages.isEmpty() ? null : updatedImages.get(0));

        // 6️⃣ Gestion des waypoints
        if (req.getWaypoints() != null) {
            if (hikingSpot.getWaypoints() == null) {
                hikingSpot.setWaypoints(new ArrayList<>());
            } else {
                hikingSpot.getWaypoints().clear();
            }

            List<HikingWaypoint> newWaypoints = req.getWaypoints().stream()
                    .map(dto -> HikingWaypoint.builder()
                            .name(dto.getName())
                            .address(dto.getAddress())
                            .placeId(dto.getPlaceId())
                            .latitude(dto.getLatitude())
                            .longitude(dto.getLongitude())
                            .rating(dto.getRating())
                            .photos(dto.getPhotos())
                            .hikingSpot(hikingSpot)
                            .build())
                    .collect(Collectors.toList());

            hikingSpot.getWaypoints().addAll(newWaypoints);
        }

        // 7️⃣ Sauvegarde finale
        hikingSpotRepository.save(hikingSpot);

        return ResponseEntity.ok(
                HikingSpotResponseDto.builder()
                        .message("✅ Hiking Spot mis à jour avec succès.")
                        .newHikingSpot(hikingSpot)
                        .build()
        );
    }

    public ResponseEntity<HikingSpotResponseDto> getHikingSpot(int id) {
        Optional<HikingSpot> findOne = hikingSpotService.getHikingSpotById(id);

        if (findOne.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(HikingSpotResponseDto.builder()
                            .message("❌ Aucun HikingSpot trouvé avec cet ID")
                            .newHikingSpot(null)
                            .build());
        }

        HikingSpot hikingSpot = findOne.get();

        return ResponseEntity.ok(HikingSpotResponseDto.builder()
                .message("✅ HikingSpot récupéré avec succès")
                .newHikingSpot(HikingSpot.builder()
                        .id(hikingSpot.getId())
                        .name(hikingSpot.getName())
                        .description(hikingSpot.getDescription())
                        .region(hikingSpot.getRegion())
                        .distance(hikingSpot.getDistance())
                        .difficultyLevel(hikingSpot.getDifficultyLevel())
                        .imagePath(hikingSpot.getImagePath())
                        .imageUrls(hikingSpot.getImageUrls())
                        .startLatitude(hikingSpot.getStartLatitude())
                        .startLongitude(hikingSpot.getStartLongitude())
                        .endLatitude(hikingSpot.getEndLatitude())
                        .endLongitude(hikingSpot.getEndLongitude())
                        .waypoints(hikingSpot.getWaypoints())
                        .build())	
                .name(hikingSpot.getCreator() != null ? hikingSpot.getCreator().getLastname() : "Inconnu")
                .build());
    }

    public ResponseEntity<HikingSpotListResponseDto> getAllHikingSpots() {
        List<HikingSpot> hikingSpots = hikingSpotService.getAllHikingSpots();

        if (hikingSpots.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<HikingSpotDto> hikingSpotDtos = hikingSpots.stream()
                .map(spot -> HikingSpotDto.builder()
                        .id(spot.getId())
                        .name(spot.getName())
                        .description(spot.getDescription())
                        .region(spot.getRegion())
                        .distance(spot.getDistance())
                        .imagePath(spot.getImagePath())
                        .imageUrls(spot.getImageUrls())
                        .startLatitude(spot.getStartLatitude())
                        .startLongitude(spot.getStartLongitude())
                        .endLatitude(spot.getEndLatitude())
                        .endLongitude(spot.getEndLongitude())
                        .difficultyLevel(spot.getDifficultyLevel())
                        .creatorName(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(HikingSpotListResponseDto.builder()
                .HikingSpots(hikingSpotDtos)
                .message("✅ Liste des HikingSpots récupérée avec succès")
                .build());
    }

    public ResponseEntity<HikingSpotResponseDto> deleteHikingSpot(int id) {
        // 1️⃣ Authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HikingSpotResponseDto.builder()
                            .message("⛔ Vous devez être connecté pour supprimer un spot.")
                            .build());
        }

        String username = authentication.getName();
        Optional<UserApp> userOptional = userRepository.findByEmail(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(HikingSpotResponseDto.builder()
                            .message("❌ Utilisateur introuvable.")
                            .build());
        }

        UserApp user = userOptional.get();

        // 2️⃣ Recherche du spot
        Optional<HikingSpot> optionalSpot = hikingSpotRepository.findById(id);
        if (optionalSpot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(HikingSpotResponseDto.builder()
                            .message("❌ HikingSpot non trouvé.")
                            .build());
        }

        HikingSpot hikingSpot = optionalSpot.get();

        // 3️⃣ Vérification des permissions
        boolean isCreator = hikingSpot.getCreator() != null &&
                hikingSpot.getCreator().getEmail().equalsIgnoreCase(user.getEmail());

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);

        if (!isCreator && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(HikingSpotResponseDto.builder()
                            .message("🚫 Vous n'êtes pas autorisé à supprimer ce spot.")
                            .build());
        }

        // 4️⃣ Supprimer uniquement les fichiers locaux (pas les photo_reference)
        if (hikingSpot.getImageUrls() != null) {
            for (String img : hikingSpot.getImageUrls()) {
                // Ne supprimer QUE les fichiers locaux
                if (img != null && !GoogleMapsUtils.isGooglePhotoReference(img) && !img.startsWith("http")) {
                    try {
                        fileStorageService.deleteFile(img);
                        System.out.println("🗑️ Fichier supprimé: " + img);
                    } catch (Exception e) {
                        System.err.println("⚠️ Erreur suppression fichier: " + img);
                    }
                }
            }
        }

        // 5️⃣ Suppression du spot
        hikingSpotRepository.deleteById(id);

        return ResponseEntity.ok(
                HikingSpotResponseDto.builder()
                        .message("🗑️ HikingSpot supprimé avec succès.")
                        .build()
        );
    }
}