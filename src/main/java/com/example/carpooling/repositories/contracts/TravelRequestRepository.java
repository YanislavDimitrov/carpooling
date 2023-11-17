package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long> {

    List<TravelRequest> findAllByStatusIs(TravelRequestStatus travelRequestStatus);

    List<TravelRequest> findByTravelIs(Travel travel);

    List<TravelRequest> findByTravelIsAndStatus(Travel travel, TravelRequestStatus status);


    TravelRequest findByTravelIsAndPassengerIsAndStatus(Travel travel, User passenger, TravelRequestStatus status);

    boolean existsTravelRequestByTravelAndPassengerAndStatus(Travel travel, User user, TravelRequestStatus status);

    boolean existsByTravelAndPassenger(Travel travel, User user);

    void deleteByTravelAndPassenger(Travel travel, User user);

    @Query("update TravelRequest  as t set t.status = 'CANCELLED' where t.travel=:travel")
    @Modifying
    @Transactional
    void updateTravelRequestAsSetStatusCancelled(Travel travel);


}
