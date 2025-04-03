package com.exemple.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemple.security.entities.Spot;
import com.exemple.security.repositories.SpotRepository;
import java.util.List;
import java.util.Optional;

@Service
public class SpotService {

    @Autowired
    private SpotRepository spotRepository;

    public List<Spot> getAllSpots() {
        return spotRepository.findAll();
    }

    public Optional<Spot> getSpotById(int id) {
        return spotRepository.findById(id);
    }

    public List<Spot> getSpotsByCreator(int creatorId) {
        return spotRepository.findByCreatorId(creatorId);
    }

    public Spot saveSpot(Spot spot) {
        return spotRepository.save(spot);
    }

    public void deleteSpot(int id) {
        spotRepository.deleteById(id);
    }
}