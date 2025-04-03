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

import com.exemple.security.dtos.HikingSpotDto;
import com.exemple.security.dtos.HikingSpotListResponseDto;
import com.exemple.security.dtos.HikingSpotRequestDto;
import com.exemple.security.dtos.HikingSpotResponseDto;
import com.exemple.security.dtos.SpotResponseDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.repositories.HikingSpotRepository;
import com.exemple.security.repositories.UserRepository;

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
	private  FileStorageService fileStorageService;
	
	public ResponseEntity<HikingSpotResponseDto> createHikingSpot(HikingSpotRequestDto req, MultipartFile imageFile){
		
		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	     String username = authentication.getName(); 
	     Optional<UserApp> userOptional = userRepository.findByEmail(username);
	     
		if (userOptional.isEmpty()) {
			return ResponseEntity.badRequest().body(HikingSpotResponseDto.builder()
					.message("Utilisateur non trouvé")
					.build());
		}
		String imagePath = null;
		UserApp user = userOptional.get();
		
		
		 if (imageFile != null && !imageFile.isEmpty()) {
	            try {
	                imagePath = fileStorageService.saveFile(imageFile); // 🔥 Sauvegarde l'image
	            } catch (Exception e) {
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(HikingSpotResponseDto.builder()
	                        .message("Erreur lors de l'enregistrement de l'image")
	                        .build());
	            }
	        }
		
		
		HikingSpot newHikingSpot = HikingSpot.builder()
				.name(req.getName())
				.description(req.getDescription())
                .startLatitude(req.getStartLatitude())
                .endLongitude(req.getEndLongitude())
                .startLongitude(req.getStartLongitude())
                .endLongitude(req.getEndLongitude())
                .imagePath(imagePath)
                .creator(user)
                .build();
		
		hikingSpotService.saveHikingSpot(newHikingSpot);
		user.getHikingSpots().add(newHikingSpot);
		userRepository.save(user);
		return ResponseEntity.ok(HikingSpotResponseDto.builder()
                .message("Hikinh Spot créé avec succès")
                .newHikingSpot(newHikingSpot)
                .build());
	}
	
	public ResponseEntity<HikingSpotResponseDto> getHikingSpot(int id){
		Optional<HikingSpot> findOne = hikingSpotService.getHikingSpotById(id);
		if (findOne.isEmpty()) {
            return ResponseEntity.status(404).body(HikingSpotResponseDto.builder()
                    .message("Aucun HikingSpot trouvé avec cet ID")
                    .newHikingSpot(null)
                    .build());
        }
		HikingSpot hikingSpot = findOne.get();
		String baseUrl = "http://localhost:8080";
		 String fullImagePath = hikingSpot.getImagePath() != null ? hikingSpot.getImagePath() : null;
		
        return ResponseEntity.ok(HikingSpotResponseDto.builder()
                .message("HikingSpot récupéré avec succès")
                .newHikingSpot(HikingSpot.builder()
                		.id(hikingSpot.getId())
                		.name(hikingSpot.getName())
                		.description(hikingSpot.getDescription())
                		.difficultyLevel(hikingSpot.getDifficultyLevel())
                		.imagePath(fullImagePath)
                		.startLatitude(hikingSpot.getStartLatitude())
                		.startLongitude(hikingSpot.getStartLongitude())
                		.endLatitude(hikingSpot.getStartLatitude())
                		.endLongitude(hikingSpot.getEndLongitude())
                		.build())
                .name(hikingSpot.getCreator().getLastname())
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
                        .imagePath(spot.getImagePath() != null ? spot.getImagePath() : null)
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
                .message("Liste trouvée")
                .build());
    }
	
	public ResponseEntity<HikingSpotResponseDto> deleteHikingSpot(int id) {
        if (!hikingSpotRepository.existsById(id)) {
            return ResponseEntity.status(404).body(HikingSpotResponseDto.builder()
                    .message("Aucun HikingSpot trouvé avec cet ID")
                    .build());
        }

        hikingSpotService.deleteHikingSpot(id);

        return ResponseEntity.ok(HikingSpotResponseDto.builder()
                .message("HikingSpot supprimé avec succès")
                .build());
    }
	
}
