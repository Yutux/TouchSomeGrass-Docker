package com.exemple.security.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.exemple.security.dtos.SpotRequestDto;
import com.exemple.security.dtos.SpotResponseDto;
import com.exemple.security.dtos.SpotsListResponseDto;
import com.exemple.security.services.SpotServiceConnector;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/spots")
public class SpotController {
	@Autowired
	private SpotServiceConnector spotService;
	
	// Créer un spot
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<SpotResponseDto> createSpot(@RequestPart("spot") SpotRequestDto spotRequestDto, @RequestPart("file") MultipartFile file) {
        return spotService.createSpot(spotRequestDto, file);
    }

    // Récupérer un spot par ID
    @GetMapping("/get/{id}")
    public ResponseEntity<SpotResponseDto> getSpot(@PathVariable int id) {
        return spotService.getSpot(id);
    }

    // Récupérer tous les spots
    @GetMapping("/get/all")
    public ResponseEntity<SpotsListResponseDto> getAllSpots() {
        return spotService.getAllSpots();
    }

    // Supprimer un spot par ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<SpotResponseDto> deleteSpot(@PathVariable int id) {
        return spotService.deleteSpot(id);
    }
}
