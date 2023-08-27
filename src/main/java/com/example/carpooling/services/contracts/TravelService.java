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

    List<Travel> getAllCompleted();

    List<Travel> findByCriteria(String driver,
                                TravelStatus status,
                                Short freeSpots,
                                LocalDateTime departureTime,
                                Sort sort);

    List<Travel> findBySearchCriteria(String departurePoint,
                                      String arrivalPoint,
                                      LocalDateTime departureTime,
                                      Short freeSpots
    );

    List<Travel> findLatestTravels();

    List<Travel> findAll(Sort sort);

    List<Travel> findTravelByUser(User user);
    void completeActiveTravelsAndDeleteUser(User user);

    List<TravelRequest> findTravelsAsPassengerByUser(User user);

    List<Travel> findAllByStatusPlanned();

    List<Travel> findPlannedTravelsWithPastDepartureTime();

    List<User> getAllPassengersForTravel(Travel travel);

    void create(Travel travel, User driver);

    Travel update(Travel travel, User editor);

    void delete(Long id, User editor);

    void completeTravel(Long id, User editor);

    void cancelTravel(Long id, User editor);

    void updateTravelStatus();
    boolean isRequestedByUser(Long travelId, User user);
    boolean isPassengerInThisTravel (User user , Travel travel);

    Long count();

    Long countCompleted();
}
