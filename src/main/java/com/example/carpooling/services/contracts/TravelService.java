package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;

import java.util.List;

public interface TravelService {

    List<Travel> get();

    Travel getById(Long id);

    List<Travel> getByDriver(User user);

    List<Travel> getByStatus(TravelStatus status);

    List<Travel> getByFreeSpots(int freeSpots);

    void create(Travel travel);

    void update(Travel travel);

    void delete(Travel travel);

    void completeTravel(Travel travel);

    void cancelTravel(Travel travel);


}
