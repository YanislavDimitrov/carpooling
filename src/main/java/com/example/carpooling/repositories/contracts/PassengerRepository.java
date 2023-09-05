package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findAllByTravelIs(Travel travel);

    Passenger findByUserAndTravel(User user, Travel travel);

    boolean existsByUserAndTravel(User user, Travel travel);

    @Modifying
    @Transactional
    @Query("update Passenger  as p set p.isActive = false where p.travel =:travel")
    void setStatusForPassengersOfATravelToInActive(Travel travel);

    @Modifying
    void deleteAllByTravel(Travel travel);
}
