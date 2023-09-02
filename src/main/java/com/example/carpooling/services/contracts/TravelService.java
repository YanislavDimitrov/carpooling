package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
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

    List<Travel> findByDriverId(Long id);

    List<Travel> findAll(Sort sort);

    List<Travel> findTravelByUser(User user);

    void completeActiveTravelsAndDeleteUser(User user);

    void completeActiveTravelsAndBlockUser(Long id, User user);

    List<TravelRequest> findTravelsAsPassengerByUser(User user);

    List<Travel> findAllByStatusPlanned();

    List<Travel> findPlannedTravelsWithPastDepartureTime();

    List<User> getAllPassengersForTravel(Travel travel);

    Page<Travel> findAllPlannedPaginated(int page,
                                         int size,
                                         Short freeSpots,
                                         LocalDate departedBefore,
                                         LocalDate departedAfter,
                                         String departurePoint,
                                         String arrivalPoint,
                                         String price,
                                         Sort sort);

    Page<Travel> findAllPaginated(int page,
                                  int size,
                                  Short freeSpots,
                                  LocalDate departedBefore,
                                  LocalDate departedAfter,
                                  String departurePoint,
                                  String arrivalPoint,
                                  String price,
                                  Sort sort);

    void create(Travel travel, User driver);

    Travel update(Travel travelToUpdate, User editor);

    void delete(Long id, User editor);

    void completeTravel(Long id, User editor);

    void cancelTravel(Long id, User editor);

    void updateTravelStatus();

    boolean isRequestedByUser(Long travelId, User user);

    boolean isPassengerInThisTravel(User user, Travel travel);

    Long count();

    Long countCompleted();


}
