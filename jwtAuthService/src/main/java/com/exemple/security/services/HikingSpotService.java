package com.exemple.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.repositories.HikingSpotRepository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HikingSpotService {

    @Autowired
    private HikingSpotRepository hikingSpotRepository;

    public List<HikingSpot> getAllHikingSpots() {
        return hikingSpotRepository.findAll();
    }

    public Optional<HikingSpot> getHikingSpotById(int id) {
        return hikingSpotRepository.findById(id);
    }

    public List<HikingSpot> getHikingSpotsByCreator(int creatorId) {
        return hikingSpotRepository.findByCreatorId(creatorId);
    }

    public HikingSpot saveHikingSpot(HikingSpot hikingSpot) {
        return hikingSpotRepository.save(hikingSpot);
    }

    public void deleteHikingSpot(int id) {
        hikingSpotRepository.deleteById(id);
    }
}
