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

import com.exemple.security.dtos.HikingSpotDto;
import com.exemple.security.dtos.HikingSpotRequestDto;
import com.exemple.security.dtos.SearchRequestDto;
import com.exemple.security.dtos.SearchResponseDto;
import com.exemple.security.services.HikingSpotServiceConnector;
import com.exemple.security.services.PhotoRefreshService;
import com.exemple.security.services.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hikingspot")
@RequiredArgsConstructor
public class HikingSpotController {

	
	 @Autowired
	 private HikingSpotServiceConnector hikingSpotServiceConnector;
	 @Autowired
	 private PhotoRefreshService photoRefreshService;
	 @Autowired
	 private SearchService searchService;

	 @PostMapping(value = "/create", consumes = "multipart/form-data")
	 public ResponseEntity<?> createHikingSpot(
	         @RequestPart("spot") HikingSpotRequestDto hikingSpotRequestDto,
	         @RequestPart(value = "files", required = false) MultipartFile[] files) {
	     return hikingSpotServiceConnector.createHikingSpot(hikingSpotRequestDto, files);
	 }

	    // ================================
	    // 🔹 GET HIKING SPOT BY ID
	    // ================================
	    @GetMapping("/get/{id}")
	    public ResponseEntity<?> getHikingSpot(@PathVariable int id) {
	        return hikingSpotServiceConnector.getHikingSpot(id);
	    }

	    // ================================
	    // 🔹 GET ALL HIKING SPOTS
	    // ================================
	    @GetMapping("/get/all")
	    public ResponseEntity<?> getAllHikingSpots() {
	        return hikingSpotServiceConnector.getAllHikingSpots();
	    }

	    // ================================
	    // 🔹 UPDATE HIKING SPOT
	    // ================================
	    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
	    public ResponseEntity<?> updateHikingSpot(
	            @PathVariable int id,
	            @RequestPart("spot") HikingSpotRequestDto hikingSpotRequestDto,
	            @RequestPart(value = "files", required = false) MultipartFile[] files) {
	        return hikingSpotServiceConnector.updateHikingSpot(id, hikingSpotRequestDto, files);
	    }

	    // ================================
	    // 🔹 DELETE HIKING SPOT
	    // ================================
	    @DeleteMapping("/delete/{id}")
	    public ResponseEntity<?> deleteHikingSpot(@PathVariable int id) {
	        return hikingSpotServiceConnector.deleteHikingSpot(id);
	    }
	    
	    /**
	     * 🔄 Refresh d'un HikingSpot
	     */
	    @PostMapping("/refresh-photos/{id}")
	    public ResponseEntity<?> refreshHikingSpotPhotos(@PathVariable int id) {
	        Map<String, Object> result = photoRefreshService.refreshHikingSpot(id);
	        return ResponseEntity.ok(result);
	    }

	    /**
	     * 🔄 Refresh de tous les HikingSpots (admin uniquement)
	     */
	    @PostMapping("/refresh-all-photos")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> refreshAllHikingSpotsPhotos() {
	        Map<String, Object> result = photoRefreshService.refreshAllHikingSpots();
	        return ResponseEntity.ok(result);
	    }
	    
	 // HikingSpotController.java
	    @PostMapping("/search")
	    public ResponseEntity<?> searchHikingSpots(
	        @RequestBody SearchRequestDto request,
	        @RequestHeader(value = "Authorization", required = false) String authHeader
	    ) {
	        SearchResponseDto<HikingSpotDto> result = searchService.searchHikingSpots(request);
	        return ResponseEntity.ok(result);
	    }
	
}
