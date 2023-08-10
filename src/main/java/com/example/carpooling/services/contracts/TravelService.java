package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface TravelService {

    List<Travel> get();

    Travel getById(Long id);

    List<Travel> getByDriver(Long id);

    List<Travel> findByCriteria(String driver,
                                TravelStatus status,
                                Short freeSpots,
                                LocalDateTime departureTime,
                                Sort sort);
    List<Travel> findAll(Sort sort);

    List<Travel> getByStatus(TravelStatus status);

    List<Travel> getByFreeSpots(int freeSpots);

    void create(Travel travel);

    void update(Long id);

    void delete(Long id);

    void completeTravel(Long id);

    void cancelTravel(Long id);


}
