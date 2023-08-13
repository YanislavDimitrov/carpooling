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

    void update(Long id);

    void delete(Long id);

    void completeTravel(Long id);

    void cancelTravel(Long id);

    TravelRequest createRequest(Travel travel, User user);

    void approveRequest(Long id);

    void rejectRequest(Long id);

    TravelRequest get(Long id);


}
