package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface TravelService {

    List<Travel> get();

    Travel getById(Long id);

    List<Travel> findByCriteria(String driver,
                                TravelStatus status,
                                Short freeSpots,
                                LocalDateTime departureTime,
                                Sort sort);

    List<Travel> findAll(Sort sort);

    Long count();

    void create(Travel travel,User driver);

    Travel update(Travel travel  , User editor);

    void delete(Long id , User editor);

    Travel completeTravel(Long id , User editor);

    void cancelTravel(Long id , User editor);




}
