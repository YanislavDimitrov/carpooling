package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRating;
import com.example.carpooling.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRatingRepository extends JpaRepository<TravelRating,Long> {

    boolean existsByUserAndTravel(User user , Travel travel);
    List<TravelRating> findByTravel(Travel travel);
}
