package com.exemple.security.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.exemple.security.dtos.HikingSpotRequestDto;

import com.exemple.security.services.HikingSpotServiceConnector;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hikingspot")
@RequiredArgsConstructor
public class HikingSpotController {

	
	 @Autowired
	 private HikingSpotServiceConnector hikingSpotServiceConnector;

	 @PostMapping(value = "/create", consumes = "multipart/form-data")
	 public ResponseEntity<?> createHikingSpot(@RequestPart("hikingSpot") HikingSpotRequestDto req, @RequestPart("file") MultipartFile file) {
		 return hikingSpotServiceConnector.createHikingSpot(req, file);
	 }

	 @GetMapping("/get/{id}")
	 public ResponseEntity<?> getHikingSpot(@PathVariable int id) {
	     return hikingSpotServiceConnector.getHikingSpot(id);
	 }

	 @GetMapping("/all")
	 public ResponseEntity<?> getAllHikingSpots() {
		 return hikingSpotServiceConnector.getAllHikingSpots();
	 }
	 
	 @DeleteMapping("/delete/{id}")
	 public ResponseEntity<?> deleteHikingSpot(@PathVariable int id) {
		 return hikingSpotServiceConnector.deleteHikingSpot(id);
	 }
	
}
