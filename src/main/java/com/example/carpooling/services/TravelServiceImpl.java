package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.InvalidOperationException;
import com.example.carpooling.exceptions.InvalidTravelException;
import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.PassengerRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelServiceImpl implements TravelService {
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d is not existing!";
    public static final String UPDATE_CANCELLED = "You cannot update this travel!";
    public static final String OPERATION_DENIED = "You are not authorized to complete this operation!";
    public static final String DELETE_TRAVEL_ERROR = "You cannot complete deleted travel!";
    public static final String COMPLETED_OR_DELETED_TRAVEL_ERROR = "You cannot cancel travel which is either completed or deleted!";
    public static final String ALREADY_STARTED_TRAVEL = "You cannot cancel your travel because it has already started!";
    public static final String EXISTING_TRAVEL = "You have a planned travel for this time frame!";
    public static final String YOU_CAN_UPDATE_ONLY_PLANNED_TRAVELS = "You can update only planned travels!";
    public static final String INVALID_STATUS = "You cannot complete travel unless it is active , if your travel is planned, consider cancelling it instead!";
    private final TravelRepository travelRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final PassengerRepository passengerRepository;
    private final UserService userService;

    private final BingMapsServiceImpl bingMapsService;

    public TravelServiceImpl(TravelRepository travelRepository, TravelRequestRepository travelRequestRepository, PassengerRepository passengerRepository, UserService userService, BingMapsServiceImpl bingMapsService) {
        this.travelRepository = travelRepository;
        this.travelRequestRepository = travelRequestRepository;
        this.passengerRepository = passengerRepository;
        this.userService = userService;
        this.bingMapsService = bingMapsService;
    }

    /**
     * @return Returns all travels which are in the database using JPA Repository findAll method
     */
    public List<Travel> get() {
        return travelRepository.getAll();
    }

    @Override
    public List<Travel> getAllCompleted() {
        return travelRepository.getAllByStatusIs(TravelStatus.COMPLETED);
    }

    @Override
    public List<Travel> findAllByStatusPlanned() {
        return travelRepository.getAllByStatusIs(TravelStatus.PLANNED);
    }

    /**
     * @param id This parameter refers to the ID of the Travel we want to return
     * @return Returns Travel entity if existing one is found
     * @Throws - EntityNotFoundException if Travel with this ID is not existing.
     */
    @Override
    public Travel getById(Long id) {
        return travelRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id)));
    }

    /**
     * @param driver        This acts as a parameter for filtering using the driver field of the travel entity
     * @param status        This acts as a parameter for filtering using the status field of the travel entity
     * @param freeSpots     This acts as a parameter for filtering using the freeSpots field of the travel entity
     * @param departureTime This acts as a parameter for filtering using the departureTime field of the travel entity
     * @param sort          This is used for sorting the result of the method
     * @return List<Travel> filled with the travels which have completed the condition if there is a filtering ,
     * otherwise returns all travels if there are any and empty list if there are not any.
     */
    @Override
    public List<Travel> findByCriteria(String driver, TravelStatus status, Short freeSpots, LocalDateTime departureTime, Sort sort) {
        return travelRepository.findByCriteria(driver, status, freeSpots, departureTime, sort);
    }

    @Override
    public List<Travel> findBySearchCriteria(String departurePoint, String arrivalPoint, LocalDateTime departureTime, Short freeSpots) {
        return travelRepository.findByCustomSearchFilter(departurePoint, arrivalPoint, departureTime, freeSpots);

    }

    @Override
    public Page<Travel> findAllPlannedPaginated(int page,
                                                int size,
                                                Short freeSpots,
                                                LocalDate departedBefore,
                                                LocalDate departedAfter,
                                                String departurePoint,
                                                String arrivalPoint,
                                                String price,
                                                Sort sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return travelRepository.findAllPlannedPaginated(pageRequest, sort, freeSpots, departedBefore, departedAfter, departurePoint, arrivalPoint, price);
    }

    @Override
    public Page<Travel> findAllPaginated(int page, int size, Short freeSpots, LocalDate departedBefore, LocalDate departedAfter, String departurePoint, String arrivalPoint, String price, Sort sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return travelRepository.findAllPaginated(pageRequest, sort, freeSpots, departedBefore, departedAfter, departurePoint, arrivalPoint, price);
    }

    /**
     * @param sort – the Sort specification to sort the results by, can be Sort.unsorted(), must not be null.
     * @return all entities sorted by the given options
     */
    public List<Travel> findAll(Sort sort) {
        return travelRepository.findAll(sort);
    }

    @Override
    public List<User> getAllPassengersForTravel(Travel travel) {
        List<Passenger> passengers = passengerRepository.findAllByTravelIs(travel);
        List<User> passengersAsUsers = new ArrayList<>();
        convertPassengerToUser(passengers, passengersAsUsers);
        return passengersAsUsers;
    }

    private static void convertPassengerToUser(List<Passenger> passengers, List<User> passengersAsUsers) {
        for (Passenger passenger : passengers) {
            passengersAsUsers.add(passenger.getUser());
        }
    }

    /**
     * @return number of entities available(status = 'ACTIVE' || 'COMPLETED') in the database.
     */


    @Override
    public Long count() {
        return travelRepository.count();
    }

    /**
     * @param travel This parameter is needed to create the travel itself
     * @param driver This parameter is needed to set the driver of the travel automatically
     *               With the help of external Microsoft Bing Maps API endpoints ,  new travel has been created.
     */
    @Override
    public void create(Travel travel, User driver) {
        checkIfTheTravelTimeFrameIsValid(travel, driver);
        travel.setStatus(TravelStatus.PLANNED);
        travel.setDriver(driver);
        travelRepository.save(travel);
    }


    @Override
    public Travel update(Travel travelToUpdate, User editor) {
        checkIfTheTravelTimeFrameIsValid(travelToUpdate, editor);
        if (!travelToUpdate.getDriver().equals(editor)) {
            throw new AuthorizationException(UPDATE_CANCELLED);
        }
        if (!travelRepository.existsById(travelToUpdate.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travelToUpdate.getId()));
        }
        if (travelToUpdate.getStatus() != TravelStatus.PLANNED) {
            throw new InvalidOperationException(YOU_CAN_UPDATE_ONLY_PLANNED_TRAVELS);
        }
        calculatingDistanceAndDuration(travelToUpdate);
        travelRepository.save(travelToUpdate);
        return travelToUpdate;
    }

    /**
     * @param id     This parameter refers to the ID of the travel we would like to delete
     * @param editor This parameter refers to the person who is trying to delete the travel
     * @Transactional - This annotation is needed for update/delete queries when using JPA Repository,
     * its default value is Propagation.REQUIRED which means if a transaction doesn't exist, a new one will be created
     * @Throws: AuthorizationException - if the editor is not admin or creator of the travel
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id, User editor) {
        if (editor.getRole() != UserRole.ADMIN && getById(id).getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        if (!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id));
        }
        Travel travel = travelRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id)));
        travel.setStatus(TravelStatus.CANCELED);
        travelRepository.delete(id);
    }

    @Override
    public List<Travel> findLatestTravels() {
        return travelRepository.findTop5ByOrderByDepartureTimeDesc();
    }

    /**
     * @param id this parameters is used to find a travel with this ID
     *           This method is changing the status of a certain travel with status 'COMPLETED'
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void completeTravel(Long id, User editor) {
        if (!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id));
        }
        Travel travel = getById(id);
        if (travel.getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        if (travel.isDeleted()) {
            throw new InvalidOperationException(DELETE_TRAVEL_ERROR);
        }
        if (travel.getStatus() != TravelStatus.ACTIVE) {
            throw new InvalidTravelException(INVALID_STATUS);
        }
        travelRepository.completeTravel(id);
    }

    /**
     * @param id this parameter is used to find a travel with this ID
     *           This method is used to change the status of a certain travel with status 'DELETED'
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void cancelTravel(Long id, User editor) {
        if (!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id));
        }
        Travel travel = getById(id);
        if (LocalDateTime.now().isAfter(travel.getDepartureTime())) {
            throw new InvalidOperationException(ALREADY_STARTED_TRAVEL);
        }
        if (travel.getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        if (travel.getStatus() != TravelStatus.PLANNED || travel.isDeleted()) {
            throw new InvalidOperationException(COMPLETED_OR_DELETED_TRAVEL_ERROR);
        }
        travel.setStatus(TravelStatus.CANCELED);
        passengerRepository.deleteAllByTravel(travel);
        travelRequestRepository.updateTravelRequestAsSetStatusCancelled(travel);
        travelRepository.save(travel);
    }

    @Override
    public List<Travel> findPlannedTravelsWithPastDepartureTime() {
        return travelRepository.findByStatusAndDepartureTimeBefore(TravelStatus.PLANNED, LocalDateTime.now());
    }


    @Override
    public Long countCompleted() {
        return travelRepository.countAllByStatusIs(TravelStatus.COMPLETED);
    }


    public List<Travel> findTravelByUser(User user) {
        return user.getTravelsAsDriver()
                .stream()
                .filter(travel -> !travel.isDeleted())
                .toList();
    }

    @Override
    public List<Travel> findByDriverId(Long id) {
        return travelRepository.findByDriver_Id(id);
    }

    public List<TravelRequest> findTravelsAsPassengerByUser(User user) {
        return user.getTravelsAsPassenger()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .toList();
    }

    public void completeActiveTravelsAndDeleteUser(User user) {
        completeActiveTravels(user);
        userService.delete(user.getId(), user);
    }


    @Override
    public void completeActiveTravelsAndBlockUser(Long id, User user) {
        completeActiveTravels(this.userService.getById(id));
        userService.block(id, user);
    }

    public boolean isRequestedByUser(Long travelId, User user) {
        Travel travel = travelRepository.findById(travelId).orElse(null);
        return travel != null && travel.getTravelRequests().stream().anyMatch(request -> request.getPassenger().equals(user));
    }

    @Override
    public boolean isPassengerInThisTravel(User user, Travel travel) {
        return passengerRepository.existsByUserAndTravel(user, travel);
    }

    public static void checkIfTheTravelTimeFrameIsValid(Travel oldTravel, Travel travel, User driver) {
        List<Travel> travelsToCheck = driver.getTravelsAsDriver();
        travelsToCheck.remove(oldTravel);

        for (Travel travelToCheck : travelsToCheck) {
            if (travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                    && travelToCheck.getStatus() == TravelStatus.ACTIVE
            ) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
            if (travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival()) &&
                    travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime()) &&
                    travelToCheck.getStatus() == TravelStatus.PLANNED) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
            if (travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival()) && travelToCheck.getStatus() == TravelStatus.PLANNED) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
            if (travel.getDepartureTime().isEqual(travelToCheck.getDepartureTime())) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
        }
        List<TravelRequest> travelRequestOfTheDriverAsPassenger = driver.getTravelsAsPassenger()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .toList();
        List<Travel> travelsOfUserAsPassenger = new ArrayList<>();
        for (TravelRequest travelRequest : travelRequestOfTheDriverAsPassenger) {
            travelsOfUserAsPassenger.add(travelRequest.getTravel());
        }
        for (Travel travelToCheckAsPassenger : travelsOfUserAsPassenger) {
            if (travel.getDepartureTime().isAfter(travelToCheckAsPassenger.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheckAsPassenger.getEstimatedTimeOfArrival()) && travelToCheckAsPassenger.getStatus() == TravelStatus.ACTIVE) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
        }
    }

    public static void checkIfTheTravelTimeFrameIsValid(Travel travel, User driver) {
        for (Travel travelToCheck : driver.getTravelsAsDriver()) {
            if (travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                    && travelToCheck.getStatus() == TravelStatus.ACTIVE
            ) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
            if (travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival()) &&
                    travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime()) &&
                    travelToCheck.getStatus() == TravelStatus.PLANNED) {
                throw new InvalidOperationException(EXISTING_TRAVEL);

            }
            if (travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                    && travelToCheck.getStatus() == TravelStatus.PLANNED) {
                throw new InvalidOperationException(EXISTING_TRAVEL);

            }
            if (travel.getDepartureTime().isEqual(travelToCheck.getDepartureTime())) {
                throw new InvalidOperationException(EXISTING_TRAVEL);

            }
            if (travel.getEstimatedTimeOfArrival().isAfter(travelToCheck.getDepartureTime()) &&
                    travel.getEstimatedTimeOfArrival().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                    && travelToCheck.getStatus() == TravelStatus.PLANNED) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
            if (travel.getEstimatedTimeOfArrival().isAfter(travelToCheck.getDepartureTime()) &&
                    travel.getEstimatedTimeOfArrival().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                    && travelToCheck.getStatus() == TravelStatus.ACTIVE) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
        }
        List<TravelRequest> travelRequestOfTheDriverAsPassenger = driver.getTravelsAsPassenger()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .toList();
        List<Travel> travelsOfUserAsPassenger = new ArrayList<>();
        for (TravelRequest travelRequest : travelRequestOfTheDriverAsPassenger) {
            travelsOfUserAsPassenger.add(travelRequest.getTravel());
        }
        for (Travel travelToCheckAsPassenger : travelsOfUserAsPassenger) {
            if (travel.getDepartureTime().isAfter(travelToCheckAsPassenger.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheckAsPassenger.getEstimatedTimeOfArrival()) && travelToCheckAsPassenger.getStatus() == TravelStatus.ACTIVE) {
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
        }
    }

    private void calculatingDistanceAndDuration(Travel travel) {
        String departurePoint = bingMapsService.getLocationJson(travel.getDeparturePoint());
        double[] coordinatesOfDeparturePoint = bingMapsService.parseCoordinates(departurePoint);
        double departureLatitude = coordinatesOfDeparturePoint[0];
        double departureLongitude = coordinatesOfDeparturePoint[1];

        String arrivalPoint = bingMapsService.getLocationJson(travel.getArrivalPoint());
        double[] coordinatesOfArrivalPoint = bingMapsService.parseCoordinates(arrivalPoint);
        double arrivalLatitude = coordinatesOfArrivalPoint[0];
        double arrivalLongitude = coordinatesOfArrivalPoint[1];

        String departurePointInCoordinates = String.format("%f,%f", departureLatitude, departureLongitude);
        String arrivalPointInCoordinates = String.format("%f,%f", arrivalLatitude, arrivalLongitude);

        int intervalBetweenDurationAndDistance = bingMapsService.getTravelDistance(
                departurePointInCoordinates, arrivalPointInCoordinates).indexOf('m');

        travel.setTravelDuration(bingMapsService.getTravelDistance(
                        departurePointInCoordinates, arrivalPointInCoordinates)
                .substring(intervalBetweenDurationAndDistance + 1));
        travel.setDistance(bingMapsService.getTravelDistance(departurePointInCoordinates, arrivalPointInCoordinates)
                .substring(0, intervalBetweenDurationAndDistance + 1));
        int indexOfMinutes = travel.getTravelDuration().indexOf('m');
        double minutesToAdd = Double.parseDouble(travel.getTravelDuration().substring(0, indexOfMinutes - 1));
        long secondsToAdd = (long) (minutesToAdd * 60);
        LocalDateTime arrivalTime = travel.getDepartureTime().plusSeconds(secondsToAdd);
        travel.setEstimatedTimeOfArrival(arrivalTime);
    }


    @Override
    @Scheduled(fixedRate = 60000)
    public void updateTravelStatus() {
        List<Travel> plannedTravels = findPlannedTravelsWithPastDepartureTime();
        for (Travel travel : plannedTravels) {
            if (travel.getDepartureTime().isBefore(LocalDateTime.now())) {
                travel.setStatus(TravelStatus.ACTIVE);
                travelRepository.save(travel);
            }
        }
    }

    private static void completeActiveTravels(User user) {
        List<Travel> travels = user.getTravelsAsDriver();
        travels = travels.stream()
                .filter(travel -> travel.getStatus() == TravelStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Travel travel : travels) {
            travel.setStatus(TravelStatus.COMPLETED);
            travel.setDeleted(true);
        }
    }
}

