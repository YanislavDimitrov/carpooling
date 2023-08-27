package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long> {

    List<TravelRequest> findAllByStatusIs(TravelRequestStatus travelRequestStatus);

    List<TravelRequest> findByTravelIs(Travel travel);


    TravelRequest findByTravelIsAndPassengerIsAndStatus(Travel travel, User passenger, TravelRequestStatus status);

    boolean existsTravelRequestByTravelAndPassengerAndStatus(Travel travel, User user, TravelRequestStatus status);
    boolean existsByTravelAndPassenger(Travel travel , User user);


}
