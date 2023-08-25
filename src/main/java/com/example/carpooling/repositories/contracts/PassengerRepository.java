package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findAllByTravelIs(Travel travel);
}
