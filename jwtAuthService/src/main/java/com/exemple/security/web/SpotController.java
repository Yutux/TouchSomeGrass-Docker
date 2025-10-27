package com.exemple.security.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.exemple.security.dtos.SearchRequestDto;
import com.exemple.security.dtos.SearchResponseDto;
import com.exemple.security.dtos.SpotDto;
import com.exemple.security.dtos.SpotRequestDto;
import com.exemple.security.dtos.SpotResponseDto;
import com.exemple.security.services.PhotoRefreshService;
import com.exemple.security.services.SearchService;
import com.exemple.security.services.SpotServiceConnector;


@RestController
@RequestMapping("/api/v1/spots")
public class SpotController {
	@Autowired
	private SpotServiceConnector spotService;
	
	@Autowired
    private PhotoRefreshService photoRefreshService;
	
	@Autowired
	private SearchService searchService;
	
	// Créer un spot
	@PostMapping(value = "/create", consumes = "multipart/form-data")
	public ResponseEntity<SpotResponseDto> createSpot(
	        @RequestPart("spot") SpotRequestDto spotRequestDto,
	        @RequestPart(value = "files", required = false) MultipartFile[] files) {
	    return spotService.createSpot(spotRequestDto, files);
	}
	
	@GetMapping("/get/{id}")
    public ResponseEntity<?> getSpot(@PathVariable int id) {
        return spotService.getSpot(id);
    }

    // ================================
    // 🔹 GET ALL SPOTS
    // ================================
    @GetMapping("/get/all")
    public ResponseEntity<?> getAllSpots() {
        return spotService.getAllSpots();
    }

    // ================================
    // 🔹 UPDATE SPOT
    // ================================
    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateSpot(
            @PathVariable int id,
            @RequestPart("spot") SpotRequestDto spotRequestDto,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return spotService.updateSpot(id, spotRequestDto, files);
    }

    // ================================
    // 🔹 DELETE SPOT
    // ================================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSpot(@PathVariable int id) {
        return spotService.deleteSpot(id);
    }
    
    /**
     * 🔄 Refresh d'un Spot
     */
    @PostMapping("/refresh-photos/{id}")
    public ResponseEntity<?> refreshSpotPhotos(@PathVariable int id) {
        // Vérifier les permissions dans PhotoRefreshService ou ici
        Map<String, Object> result = photoRefreshService.refreshSpot(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 🔄 Refresh de tous les Spots (admin uniquement)
     */
    @PostMapping("/refresh-all-photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refreshAllSpotsPhotos() {
        Map<String, Object> result = photoRefreshService.refreshAllSpots();
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/search")
    public ResponseEntity<?> searchSpots(
        @RequestBody SearchRequestDto request,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // La recherche fonctionne avec ou sans token
        SearchResponseDto<SpotDto> result = searchService.searchSpots(request);
        return ResponseEntity.ok(result);
    }
}
