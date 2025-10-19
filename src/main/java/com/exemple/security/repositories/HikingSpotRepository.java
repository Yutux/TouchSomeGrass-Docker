package com.exemple.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.exemple.security.entities.HikingSpot;
import java.util.List;

@Repository
public interface HikingSpotRepository extends JpaRepository<HikingSpot, Integer> {
    List<HikingSpot> findByCreatorId(int creatorId);
}
