package com.exemple.security.services;

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
	
	public ResponseEntity<SpotResponseDto> createSpot(SpotRequestDto spot, MultipartFile imageFile){
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	     String username = authentication.getName(); 
	     Optional<UserApp> userOptional = userRepository.findByEmail(username);
		
		if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(SpotResponseDto.builder()
                    .message("Utilisateur non trouvé")
                    .build());
        }
		
		String imagePath = null;
		UserApp user = userOptional.get();
		
		 // Enregistrer l'image si elle est fournie
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imagePath = fileStorageService.saveFile(imageFile); // Sauvegarde l'image
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SpotResponseDto.builder()
                        .message("Erreur lors de l'enregistrement de l'image")
                        .build());
            }
        }
		
		 Spot newSpot = Spot.builder()
	                .name(spot.getName())
	                .description(spot.getDescription())
	                .latitude(spot.getLatitude())
	                .longitude(spot.getLongitude())
	                .imagePath(imagePath)
	                .creator(user)
	                .build();

		 spotService.saveSpot(newSpot);
		 user.getSpots().add(newSpot);
		 userRepository.save(user);
	     return ResponseEntity.ok(SpotResponseDto.builder()
	                .message("Spot créé avec succès")
	                .newSpot(newSpot)
	                .build());
	}
	
	public ResponseEntity<SpotResponseDto> getSpot(int id){
		Optional<Spot> findOne = spotService.getSpotById(id);
		
		if (findOne.isEmpty()) {
            return ResponseEntity.status(404).body(SpotResponseDto.builder()
                    .message("Aucun HikingSpot trouvé avec cet ID")
                    .newSpot(null)
                    .build());
        }
		
		Spot spot = findOne.get();
		 String fullImagePath = spot.getImagePath() != null ? spot.getImagePath() : null;

        return ResponseEntity.ok(SpotResponseDto.builder()
                .message("HikingSpot récupéré avec succès")
                .newSpot(Spot.builder()
                        .id(spot.getId())
                        .name(spot.getName())
                        .description(spot.getDescription())
                        .latitude(spot.getLatitude())
                        .longitude(spot.getLongitude())
                        .imagePath(fullImagePath) // URL complète
                        .creator(spot.getCreator())
                        .build())
                .creatorname(spot.getCreator().getLastname())
                .build());
		
	}
	
	
	public ResponseEntity<SpotsListResponseDto> getAllSpots() {
        List<Spot> spots = spotService.getAllSpots();
        
        
        if (spots.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content si liste vide
        }
        
        List<SpotDto> SpotDtos = spots.stream()
                .map(spot -> SpotDto.builder()
                        .id(spot.getId())
                        .name(spot.getName())
                        .description(spot.getDescription())
                        .imagePath(spot.getImagePath() != null ? spot.getImagePath() : null)
                        .latitude(spot.getLatitude())
                        .longitude(spot.getLongitude())
                        .creator(spot.getCreator() != null ? spot.getCreator().getLastname() : "Inconnu") // Ajouter le nom du créateur
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(SpotsListResponseDto.builder()
                .message("Liste des spots récupérée avec succès")
                .spots(SpotDtos)
                .build());
    }
	
	 public ResponseEntity<SpotResponseDto> deleteSpot(int id) {
	        if (!spotRepository.existsById(id)) {
	            
	            return ResponseEntity.status(404).body(SpotResponseDto.builder()
	                    .message("Aucun spot trouvé avec cet ID")
	                    .newSpot(null)
	                    .build());
	        }

	        try {
	            Spot spot = spotRepository.findById(id).orElse(null);
	            spotRepository.deleteById(id);

	            return ResponseEntity.ok(SpotResponseDto.builder()
	                    .message("Spot supprimé avec succès")
	                    .newSpot(spot)
	                    .build());

	        } catch (Exception e) {
	            
	            return ResponseEntity.status(500).body(SpotResponseDto.builder()
	                    .message("Erreur interne lors de la suppression du spot")
	                    .newSpot(null)
	                    .build());
	        }
	 }
}
