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

import com.exemple.security.dtos.SpotDto;
import com.exemple.security.dtos.SpotRequestDto;
import com.exemple.security.dtos.SpotResponseDto;
import com.exemple.security.dtos.SpotsListResponseDto;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.SpotRepository;
import com.exemple.security.repositories.UserRepository;

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
	
	public ResponseEntity<SpotResponseDto> createSpot(SpotRequestDto spot, MultipartFile[] imageFiles) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    System.out.println(username);
	    Optional<UserApp> userOptional = userRepository.findByEmail(username);

	    if (userOptional.isEmpty()) {
	        return ResponseEntity.badRequest().body(
	            SpotResponseDto.builder()
	                .message("Utilisateur non trouvé")
	                .build()
	        );
	    }

	    UserApp user = userOptional.get();
	    List<String> allImagePaths = new ArrayList<>();

	    // 🌐 1️⃣ Ajouter les URLs d’images Google Maps
	    if (spot.getImageUrls() != null && !spot.getImageUrls().isEmpty()) {
	        allImagePaths.addAll(spot.getImageUrls());
	    }

	    // 📁 2️⃣ Ajouter les images uploadées
	    if (imageFiles != null && imageFiles.length > 0) {
	        for (MultipartFile file : imageFiles) {
	            if (!file.isEmpty()) {
	                try {
	                    String savedPath = fileStorageService.saveFile(file);
	                    allImagePaths.add(savedPath);
	                } catch (Exception e) {
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                        SpotResponseDto.builder()
	                            .message("Erreur lors de l'enregistrement d'une image")
	                            .build()
	                    );
	                }
	            }
	        }
	    }

	    // 🖼️ 3️⃣ Sélectionner l’image principale
	    String mainImage = null;
	    if (imageFiles != null && imageFiles.length > 0) {
	        try {
	            mainImage = fileStorageService.saveFile(imageFiles[0]);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(SpotResponseDto.builder()
	                    .message("Erreur lors de l'enregistrement de l'image principale")
	                    .build());
	        }
	    }

	    // ✅ Construction
	    Spot newSpot = Spot.builder()
	        .name(spot.getName())
	        .description(spot.getDescription())
	        .latitude(spot.getLatitude())
	        .longitude(spot.getLongitude())
	        .imagePath(mainImage)
	        .imageUrls(allImagePaths)
	        .creator(user)
	        .build();

	    // 💾 5️⃣ Sauvegarde
	    spotService.saveSpot(newSpot);
	    user.getSpots().add(newSpot);
	    userRepository.save(user);

	    return ResponseEntity.ok(
	        SpotResponseDto.builder()
	            .message("Spot créé avec succès avec " + allImagePaths.size() + " image(s)")
	            .newSpot(newSpot)
	            .build()
	    );
	}
	
	public ResponseEntity<SpotResponseDto> updateSpot(int id, SpotRequestDto req, MultipartFile[] imageFiles) {
	    // 1️⃣ Vérifie que l’utilisateur est authentifié
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
	                        .message("Utilisateur introuvable.")
	                        .build());
	    }

	    UserApp user = userOptional.get();

	    // 2️⃣ Recherche du spot
	    Optional<Spot> optionalSpot = spotRepository.findById(id);
	    if (optionalSpot.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(SpotResponseDto.builder()
	                        .message("Spot non trouvé.")
	                        .build());
	    }

	    Spot spot = optionalSpot.get();

	    // 3️⃣ Vérifie que l’utilisateur est autorisé : créateur ou admin
	    boolean isCreator = spot.getCreator() != null &&
	            spot.getCreator().getEmail().equalsIgnoreCase(user.getEmail());

	    boolean isAdmin = user.getRoles().stream()
	            .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);

	    if (!isCreator && !isAdmin) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(SpotResponseDto.builder()
	                        .message("🚫 Vous n’êtes pas autorisé à modifier ce spot.")
	                        .build());
	    }

	    // 4️⃣ Met à jour les champs
	    spot.setName(req.getName() != null ? req.getName() : spot.getName());
	    spot.setDescription(req.getDescription() != null ? req.getDescription() : spot.getDescription());
	    spot.setLatitude(req.getLatitude());
	    spot.setLongitude(req.getLongitude());

	    // 5️⃣ Gestion des images
	    List<String> currentImages = spot.getImageUrls() != null ? new ArrayList<>(spot.getImageUrls()) : new ArrayList<>();
	    List<String> updatedImages = req.getImageUrls() != null ? new ArrayList<>(req.getImageUrls()) : new ArrayList<>();

	    // ➖ Supprimer les images retirées
	    List<String> removed = currentImages.stream()
	            .filter(img -> !updatedImages.contains(img))
	            .collect(Collectors.toList());

	    for (String img : removed) {
	        if (!img.startsWith("http")) {
	            try {
	                fileStorageService.deleteFile(img);
	            } catch (Exception e) {
	                System.err.println("Erreur suppression fichier : " + img);
	            }
	        }
	    }

	    // ➕ Ajouter les nouvelles images uploadées
	    if (imageFiles != null && imageFiles.length > 0) {
	        for (MultipartFile file : imageFiles) {
	            if (!file.isEmpty()) {
	                try {
	                    String newPath = fileStorageService.saveFile(file);
	                    updatedImages.add(newPath);
	                } catch (Exception e) {
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                            .body(SpotResponseDto.builder()
	                                    .message("Erreur lors de l'upload d'une image : " + e.getMessage())
	                                    .build());
	                }
	            }
	        }
	    }

	    spot.setImageUrls(updatedImages);
	    spot.setImagePath(updatedImages.isEmpty() ? null : updatedImages.get(0));

	    spotRepository.save(spot);

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SpotResponseDto.builder()
                    .message("Aucun Spot trouvé avec cet ID")
                    .newSpot(null)
                    .build());
        }

        Spot spot = findOne.get();

        return ResponseEntity.ok(SpotResponseDto.builder()
                .message("Spot récupéré avec succès")
                .newSpot(Spot.builder()
                        .id(spot.getId())
                        .name(spot.getName())
                        .description(spot.getDescription())
                        .latitude(spot.getLatitude())
                        .longitude(spot.getLongitude())
                        .imagePath(spot.getImagePath())
                        .imageUrls(spot.getImageUrls()) // ✅ toutes les images
                        .creator(spot.getCreator())
                        .build())
                .creatorname(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                .build());
    }
	
	
	public ResponseEntity<SpotsListResponseDto> getAllSpots() {
        List<Spot> spots = spotService.getAllSpots();

        if (spots.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<SpotDto> spotDtos = spots.stream()
                .map(spot -> SpotDto.builder()
                        .id(spot.getId())
                        .name(spot.getName())
                        .description(spot.getDescription())
                        .latitude(spot.getLatitude())
                        .longitude(spot.getLongitude())
                        .imagePath(spot.getImagePath())
                        .imageUrls(spot.getImageUrls()) // ✅ Ajout
                        .creator(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu")
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(SpotsListResponseDto.builder()
                .message("Liste des spots récupérée avec succès")
                .spots(spotDtos)
                .build());
    }
	
	public ResponseEntity<SpotResponseDto> deleteSpot(int id) {
	    // 1️⃣ Vérifie que l’utilisateur est authentifié
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
	                        .message("Utilisateur introuvable.")
	                        .build());
	    }

	    UserApp user = userOptional.get();

	    // 2️⃣ Recherche du spot
	    Optional<Spot> optionalSpot = spotRepository.findById(id);
	    if (optionalSpot.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(SpotResponseDto.builder()
	                        .message("Aucun spot trouvé avec cet ID.")
	                        .build());
	    }

	    Spot spot = optionalSpot.get();

	    // 3️⃣ Vérifie que l’utilisateur est autorisé : créateur ou admin
	    boolean isCreator = spot.getCreator() != null &&
	            spot.getCreator().getEmail().equalsIgnoreCase(user.getEmail());

	    boolean isAdmin = user.getRoles().stream()
	            .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);

	    if (!isCreator && !isAdmin) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(SpotResponseDto.builder()
	                        .message("🚫 Vous n’êtes pas autorisé à supprimer ce spot.")
	                        .build());
	    }

	    // 4️⃣ Supprime les fichiers locaux
	    if (spot.getImageUrls() != null) {
	        for (String img : spot.getImageUrls()) {
	            if (img != null && !img.startsWith("http")) {
	                try {
	                    fileStorageService.deleteFile(img);
	                } catch (Exception e) {
	                    System.err.println("Erreur lors de la suppression du fichier : " + img);
	                }
	            }
	        }
	    }

	    // 5️⃣ Supprime le spot
	    spotRepository.deleteById(id);

	    return ResponseEntity.ok(
	            SpotResponseDto.builder()
	                    .message("🗑️ Spot supprimé avec succès.")
	                    .build()
	    );
	}

}
