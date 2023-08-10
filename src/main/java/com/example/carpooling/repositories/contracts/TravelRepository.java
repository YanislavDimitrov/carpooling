package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelRepository  extends JpaRepository<Travel,Long> {

    List<Travel> findAllByDriverIs(User user) throws EntityNotFoundException;
}
