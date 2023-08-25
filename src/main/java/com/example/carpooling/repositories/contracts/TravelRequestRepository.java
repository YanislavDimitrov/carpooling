package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long> {

    List<TravelRequest>  findAllByStatusIs(TravelRequestStatus travelRequestStatus);
    List<TravelRequest> findByTravelIs(Travel travel);
    TravelRequest findByTravelIsAndPassengerIs(Travel travel , User passenger);
}
