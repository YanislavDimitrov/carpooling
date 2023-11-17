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

/**
 * /**
 * * The `TravelServiceImpl` class provides services related to travel management.
 * * It allows creating, updating, canceling, and completing travel, as well as retrieving travel information.
 * * This class interacts with repositories and external services to perform these operations.
 * *
 * * @author Ivan Boev
 * * @version 1.0
 * * @since 2023-09-04
 */

@Service
public class TravelServiceImpl implements TravelService {

    // Constants for error messages
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d is not existing!";
    public static final String UPDATE_CANCELLED = "You cannot update this travel!";
    public static final String OPERATION_DENIED = "You are not authorized to complete this operation!";
    public static final String DELETE_TRAVEL_ERROR = "You cannot complete deleted travel!";
    public static final String COMPLETED_OR_DELETED_TRAVEL_ERROR = "You cannot cancel travel which is either completed or deleted!";
    public static final String ALREADY_STARTED_TRAVEL = "You cannot cancel your travel because it has already started!";
    public static final String EXISTING_TRAVEL = "You have a planned travel for this time frame!";
    public static final String YOU_CAN_UPDATE_ONLY_PLANNED_TRAVELS = "You can update only planned travels!";
    public static final String INVALID_STATUS = "You cannot complete travel unless it is active , if your travel is planned, consider cancelling it instead!";

    //Dependencies
    private final TravelRepository travelRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final PassengerRepository passengerRepository;
    private final UserService userService;
    private final BingMapsServiceImpl bingMapsService;

    /**
     * Constructs a new `TravelServiceImpl` with the specified repositories and services.
     *
     * @param travelRepository        The repository for managing travel entities.
     * @param travelRequestRepository The repository for managing travel requests.
     * @param passengerRepository     The repository for managing passengers.
     * @param userService             The service for managing users.
     * @param bingMapsService         The service for interacting with Bing Maps.
     */
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

    /**
     * @return All travels which are with status - COMPLETED.
     */
    @Override
    public List<Travel> getAllCompleted() {
        return travelRepository.getAllByStatusIs(TravelStatus.COMPLETED);
    }

    /**
     * @return All travels which are with status - PLANNED.
     */
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

    /**
     * Searches for travels that match the specified search criteria, including departure point,
     * arrival point, departure time, and the number of free spots.
     *
     * @param departurePoint The starting point of the travel.
     * @param arrivalPoint   The destination point of the travel.
     * @param departureTime  The departure time of the travel.
     * @param freeSpots      The number of available free spots in the travel.
     * @return A list of travels that match the specified search criteria.
     */
    @Override
    public List<Travel> findBySearchCriteria(String departurePoint, String arrivalPoint, LocalDateTime departureTime, Short freeSpots) {
        return travelRepository.findByCustomSearchFilter(departurePoint, arrivalPoint, departureTime, freeSpots);

    }

    /**
     * Retrieves a paginated list of planned travels based on various filtering criteria.
     *
     * @param page           The page number of the results to retrieve.
     * @param size           The number of results per page.
     * @param freeSpots      The number of available free spots in the travel.
     * @param departedBefore The maximum departure date for the travels to include.
     * @param departedAfter  The minimum departure date for the travels to include.
     * @param departurePoint The starting point of the travel.
     * @param arrivalPoint   The destination point of the travel.
     * @param price          The price range for the travels to include.
     * @param sort           The sorting criteria for the results.
     * @return A paginated list of planned travels that meet the specified criteria.
     */
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

    /**
     * Retrieves a paginated list of travels based on various filtering criteria.
     *
     * @param page           The page number of the results to retrieve.
     * @param size           The number of results per page.
     * @param freeSpots      The number of available free spots in the travel.
     * @param departedBefore The maximum departure date for the travels to include.
     * @param departedAfter  The minimum departure date for the travels to include.
     * @param departurePoint The starting point of the travel.
     * @param arrivalPoint   The destination point of the travel.
     * @param price          The price range for the travels to include.
     * @param sort           The sorting criteria for the results.
     * @return A paginated list of travels that meet the specified criteria.
     */
    @Override
    public Page<Travel> findAllPaginated(int page, int size, Short freeSpots, LocalDate departedBefore, LocalDate departedAfter, String departurePoint, String arrivalPoint, String price, Sort sort) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return travelRepository.findAllPaginated(pageRequest, sort, freeSpots, departedBefore, departedAfter, departurePoint, arrivalPoint, price);
    }

    /**
     * @param sort – the Sort specification to sort the results by, can be Sort.unsorted(), must not be null.
     * @return All entities sorted by the given options
     */
    public List<Travel> findAll(Sort sort) {
        return travelRepository.findAll(sort);
    }

    /**
     * Retrieves a list of passengers for the specified travel.
     *
     * @param travel The travel for which to retrieve passengers.
     * @return A list of users who are passengers on the given travel.
     */
    @Override
    public List<User> getAllPassengersForTravel(Travel travel) {
        List<Passenger> passengers = passengerRepository.findAllByTravelIs(travel);
        List<User> passengersAsUsers = new ArrayList<>();
        convertPassengerToUser(passengers, passengersAsUsers);
        return passengersAsUsers;
    }

    /**
     * Converts a list of passenger entities to a list of user entities.
     *
     * @param passengers        The list of passenger entities to convert.
     * @param passengersAsUsers The list to which converted user entities will be added.
     */
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
     * Creates a new travel with the provided details and associates it with the specified driver.
     *
     * @param travel The travel entity to be created.
     * @param driver The user who will be the driver of the new travel.
     * @throws InvalidOperationException If there is a time conflict with other travels for the same driver.
     */
    @Override
    public void create(Travel travel, User driver) {
        //Check for any potential time conflicts
        checkIfTheTravelTimeFrameIsValidWithQuery(travel, driver);

        //Set properties for travel
        travel.setStatus(TravelStatus.PLANNED);
        travel.setDriver(driver);

        // Save the created travel entity
        travelRepository.save(travel);
    }

    /**
     * Updates the details of a travel if the editor is authorized to do so.
     *
     * @param travelToUpdate The travel entity containing the updated information.
     * @param editor         The user attempting to edit the travel.
     * @return The updated travel entity.
     * @throws AuthorizationException    If the editor is not authorized to update the travel.
     * @throws EntityNotFoundException   If the travel with the specified ID does not exist.
     * @throws InvalidOperationException If the travel status is not "PLANNED," or if there is a time conflict with other travels.
     */
    @Override
    public Travel update(Travel travelToUpdate, User editor) {
        // Check if the editor is the driver of the travel
        if (!travelToUpdate.getDriver().equals(editor)) {
            throw new AuthorizationException(UPDATE_CANCELLED);
        }
        // Check if the travel with the specified ID exists
        if (!travelRepository.existsById(travelToUpdate.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travelToUpdate.getId()));
        }
        // Check if the travel status is "PLANNED" (only planned travels can be updated)
        if (travelToUpdate.getStatus() != TravelStatus.PLANNED) {
            throw new InvalidOperationException(YOU_CAN_UPDATE_ONLY_PLANNED_TRAVELS);
        }
        // Calculate distance, duration, and check for time conflicts
        calculatingDistanceAndDuration(travelToUpdate);
        checkIfTimeIsValid(travelToUpdate, editor);
        // Save the updated travel entity
        travelRepository.save(travelToUpdate);

        return travelToUpdate;
    }

    /**
     * Check whether the travel is withing valid timeframe
     *
     * @param travel The travel which timeframe will be compared
     * @param editor The user which travels will be traversed
     */
    private void checkIfTimeIsValid(Travel travel, User editor) {
        for (Travel travelToCheck : editor.getTravelsAsDriver()) {
            checkIfArrivalTimeIsWithinInvalidTimeFrame(travel, travelToCheck);
        }
    }

    /**
     * Checks if the estimated time of arrival of a travel is within an invalid time frame when compared to another travel.
     * This method is used to detect time conflicts between two travels based on their estimated time of arrival.
     *
     * @param travel        The first travel being compared.
     * @param travelToCheck The second travel being compared.
     * @throws InvalidOperationException If the estimated time of arrival of the first travel falls within the time frame
     *                                   of the second travel, and the second travel is either "PLANNED" or "ACTIVE."
     */
    private static void checkIfArrivalTimeIsWithinInvalidTimeFrame(Travel travel, Travel travelToCheck) {
        if (travel.getEstimatedTimeOfArrival().isAfter(travelToCheck.getDepartureTime()) && travel.getEstimatedTimeOfArrival().isBefore(travelToCheck.getEstimatedTimeOfArrival()) && travelToCheck.getStatus() == TravelStatus.PLANNED) {
            throw new InvalidOperationException(EXISTING_TRAVEL);
        }
        if (travel.getEstimatedTimeOfArrival().isAfter(travelToCheck.getDepartureTime()) && travel.getEstimatedTimeOfArrival().isBefore(travelToCheck.getEstimatedTimeOfArrival()) && travelToCheck.getStatus() == TravelStatus.ACTIVE) {
            throw new InvalidOperationException(EXISTING_TRAVEL);
        }
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
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id)));
        travel.setStatus(TravelStatus.CANCELED);
        travelRepository.delete(id);
    }

    /**
     * Retrieves a list of the latest travel records based on their departure time.
     * <p>
     * This method queries the travel repository to retrieve a list of up to five travel
     * records with the most recent departure times. The records are sorted in descending
     * order of departure time, ensuring that the latest travels appear first.
     *
     * @return A list of Travel objects representing the latest travel records.
     * @see TravelRepository
     * @see Travel
     */
    @Override
    public List<Travel> findLatestTravels() {
        return travelRepository.findTop5ByOrderByDepartureTimeDesc();
    }

    /**
     * Completes a travel operation by updating its status to "completed."
     *
     * @param id     The unique identifier of the travel to be completed.
     * @param editor The user who is attempting to complete the travel.
     * @throws EntityNotFoundException   If the travel with the given ID does not exist.
     * @throws AuthorizationException    If the editor is not the driver of the travel.
     * @throws InvalidOperationException If the travel has been deleted or is in an invalid state.
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
     * Cancels a planned travel by updating its status to "canceled" if certain conditions are met.
     *
     * @param id     The unique identifier of the travel to be canceled.
     * @param editor The user who is attempting to cancel the travel.
     * @throws EntityNotFoundException   If the travel with the given ID does not exist.
     * @throws InvalidOperationException If the travel has already started, is completed, or has been deleted.
     * @throws AuthorizationException    If the editor is not the driver of the travel.
     * @see TravelStatus
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

    /**
     * Retrieves a list of planned travels that have a departure time in the past.
     *
     * @return A list of planned travels with past departure times.
     */

    @Override
    public List<Travel> findPlannedTravelsWithPastDepartureTime() {
        return travelRepository.findByStatusAndDepartureTimeBefore(TravelStatus.PLANNED, LocalDateTime.now());
    }

    /**
     * Counts the number of completed travels.
     *
     * @return The total count of completed travels.
     */
    @Override
    public Long countCompleted() {
        return travelRepository.countAllByStatusIs(TravelStatus.COMPLETED);
    }

    @Override
    public Long countOrganized() {
        return travelRepository.countAllByStatusIn(List.of(
                TravelStatus.ACTIVE,
                TravelStatus.COMPLETED,
                TravelStatus.PLANNED));
    }


    /**
     * Retrieves a list of non-deleted travels associated with a specific user who is the driver.
     *
     * @param user The user for whom to retrieve travels.
     * @return A list of non-deleted travels associated with the specified user as the driver.
     */
    public List<Travel> findTravelByUser(User user) {
        return user.getTravelsAsDriver()
                .stream()
                .filter(travel -> !travel.isDeleted())
                .toList();
    }

    /**
     * Retrieves a list of travels by searching for travels with a specific driver's ID.
     *
     * @param id The unique identifier of the driver.
     * @return A list of travels associated with the specified driver's ID.
     */
    @Override
    public List<Travel> findByDriverId(Long id) {
        return travelRepository.findByDriver_Id(id);
    }

    /**
     * Retrieves a list of approved travel requests associated with a specific user as a passenger.
     *
     * @param user The user for whom to retrieve approved travel requests.
     * @return A list of approved travel requests associated with the specified user as a passenger.
     */
    public List<TravelRequest> findTravelsAsPassengerByUser(User user) {
        return user.getTravelsAsPassenger()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .toList();
    }

    /**
     * Completes active travels associated with a specific user as the driver and deletes the user.
     *
     * @param user The user to be deleted after completing active travels.
     */
    public void completeActiveTravelsAndDeleteUser(User user) {
        completeActiveTravels(user);
        userService.delete(user.getId(), user);
    }

    /**
     * Completes active travels associated with a specific user as the driver and blocks the user.
     *
     * @param id   The unique identifier of the user to be blocked after completing active travels.
     * @param user The user initiating the action.
     */
    @Override
    public void completeActiveTravelsAndBlockUser(Long id, User user) {
        completeActiveTravels(this.userService.getById(id));
        userService.block(id, user);
    }

    /**
     * Checks if a user has requested to join a specific travel.
     *
     * @param travelId The unique identifier of the travel to be checked.
     * @param user     The user for whom the request is being checked.
     * @return True if the user has requested to join the travel, otherwise false.
     */
    public boolean isRequestedByUser(Long travelId, User user) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new EntityNotFoundException("id", travelId));
        return this.travelRequestRepository
                .existsTravelRequestByTravelAndPassengerAndStatus(travel, user,TravelRequestStatus.PENDING);
    }

    /**
     * Checks if a user is a passenger in a specific travel.
     *
     * @param user   The user to check as a passenger.
     * @param travel The travel to check for the user's presence as a passenger.
     * @return True if the user is a passenger in the travel, otherwise false.
     */
    @Override
    public boolean isPassengerInThisTravel(User user, Travel travel) {
        return passengerRepository.existsByUserAndTravel(user, travel);
    }

    @Override
    public boolean isPassengerRejected(User loggedUser, Travel travel) {
        return this.travelRequestRepository
                .existsTravelRequestByTravelAndPassengerAndStatus(travel, loggedUser, TravelRequestStatus.REJECTED);
    }

    /**
     * Checks if the time frame of a travel is valid by comparing it to other travels of the same driver and passenger history.
     *
     * @param travel The travel for which the time frame is being checked.
     * @param driver The user who is the driver of the travel.
     */
    public static void checkIfTheTravelTimeFrameIsValid(Travel travel, User driver) {
        for (Travel travelToCheck : driver.getTravelsAsDriver()) {
            checkIfDepartureTimeIsWithingValidTimeFrame(travel, travelToCheck);
            checkIfArrivalTimeIsWithinInvalidTimeFrame(travel, travelToCheck);
        }
        checkIfThereAreAnyTravelsAsPassengerWithingThisTimeFrame(travel, driver);
    }

    /**
     * Checks if there are any travels for a specified user as a passenger that overlap in time with the provided travel.
     * <p>
     * This method retrieves the list of travel requests made by the specified driver as a passenger, filters out only the approved requests,
     * and then checks if there are any active travels of the user as a passenger that overlap in time with the provided travel.
     *
     * @param travel The travel for which the time overlap is being checked.
     * @param driver The user (driver) whose travel requests as a passenger are considered for overlap checking.
     * @throws InvalidOperationException if there is an existing travel for the specified user as a passenger that overlaps in time
     *                                   with the provided travel.
     */
    private static void checkIfThereAreAnyTravelsAsPassengerWithingThisTimeFrame(Travel travel, User driver) {
        // Retrieve the list of approved travel requests made by the specified driver as a passenger.
        List<TravelRequest> travelRequestOfTheDriverAsPassenger = driver.getTravelsAsPassenger()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .toList();

        // Create a list to store the actual travels associated with the approved travel requests.
        List<Travel> travelsOfUserAsPassenger = new ArrayList<>();
        // Populate the list of actual travels from the approved travel requests.
        for (TravelRequest travelRequest : travelRequestOfTheDriverAsPassenger) {
            travelsOfUserAsPassenger.add(travelRequest.getTravel());
        }

        // Check for time overlaps between the provided travel and the user's travels as a passenger.
        for (Travel travelToCheckAsPassenger : travelsOfUserAsPassenger) {
            if (travel.getDepartureTime().isAfter(travelToCheckAsPassenger.getDepartureTime())
                    && travel.getDepartureTime().isBefore(travelToCheckAsPassenger.getEstimatedTimeOfArrival())
                    && travelToCheckAsPassenger.getStatus() == TravelStatus.ACTIVE) {
                // Throw an exception if there is an existing travel with a time overlap.
                throw new InvalidOperationException(EXISTING_TRAVEL);
            }
        }
    }

    /**
     * Checks if the departure time of a provided travel falls within valid time frames when compared to another travel.
     * <p>
     * This method compares the departure time of the provided travel with the departure time and estimated time of arrival of another travel,
     * and checks whether they fall within valid time frames based on the status of the other travel. It throws an exception if any of the
     * following conditions are met:
     * 1. The departure time of the provided travel falls within the active period of the other travel.
     * 2. The departure time of the provided travel falls within the planned period of the other travel.
     * 3. The departure time of the provided travel is the same as the departure time of the other travel.
     *
     * @param travel        The travel for which the departure time is being checked.
     * @param travelToCheck The travel to compare against for valid time frames.
     * @throws InvalidOperationException if any of the specified conditions are met,
     *                                   indicating that the departure time is not within a valid time frame.
     */
    private static void checkIfDepartureTimeIsWithingValidTimeFrame(Travel travel, Travel travelToCheck) {
        // Check if the departure time of the provided travel falls within the active period of the other travel.
        if (travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime())
                && travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                && travelToCheck.getStatus() == TravelStatus.ACTIVE
        ) {
            throw new InvalidOperationException(EXISTING_TRAVEL);
        }

        // Check if the departure time of the provided travel falls within the planned period of the other travel.
        if (travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival()) &&
                travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime()) &&
                travelToCheck.getStatus() == TravelStatus.PLANNED) {
            throw new InvalidOperationException(EXISTING_TRAVEL);

        }

        // Check if the departure time of the provided travel is the same as the departure time of the other travel.
        if (travel.getDepartureTime().isAfter(travelToCheck.getDepartureTime())
                && travel.getDepartureTime().isBefore(travelToCheck.getEstimatedTimeOfArrival())
                && travelToCheck.getStatus() == TravelStatus.PLANNED) {
            throw new InvalidOperationException(EXISTING_TRAVEL);
        }
        if (travel.getDepartureTime().isEqual(travelToCheck.getDepartureTime())) {
            throw new InvalidOperationException(EXISTING_TRAVEL);

        }
    }

    /**
     * Checks if the time frame of a new travel is valid concerning the specified driver's existing travels.
     * This method validates the time frame of a new travel by comparing it against the time frames of the driver's existing travels.
     * It first removes the provided "oldTravel" from the list of travels to check, as it's typically the travel being updated.
     * Then, it iterates through the remaining travels and checks if the departure time of the new travel falls within valid time frames
     * concerning each existing travel. Additionally, it verifies if there are any existing travels where the user is a passenger that
     * overlap with the new travel's time frame.
     *
     * @param oldTravel The existing travel being updated (may be null for new travel creation).
     * @param travel    The new travel to be checked for a valid time frame.
     * @param driver    The driver whose existing travels are considered for time frame validation.
     * @throws InvalidOperationException if the new travel's departure time overlaps with the time frame of any existing travel of the driver
     *                                   or if there are any existing travels where the user is a passenger that overlap with the new travel.
     */
    public void checkIfTheTravelTimeFrameIsValid(Travel oldTravel, Travel travel, User driver) {
        List<Travel> travelsToCheck = driver.getTravelsAsDriver();
        travelsToCheck.remove(oldTravel);

        for (Travel travelToCheck : travelsToCheck) {
            checkIfDepartureTimeIsWithingValidTimeFrame(travel, travelToCheck);

        }
        checkIfThereAreAnyTravelsAsPassengerWithingThisTimeFrame(travel, driver);
    }

    /**
     * Checks if the time frame of a new travel is valid concerning the specified driver's existing travels using a database query.
     * <p>
     * This method validates the time frame of a new travel by querying the database to count the number of existing travels for the
     * specified driver that conflict with the new travel's time frame. If any conflicting travels are found, an exception is thrown,
     * indicating that the new travel's time frame is invalid.
     *
     * @param travel The new travel to be checked for a valid time frame.
     * @param driver The driver whose existing travels are considered for time frame validation.
     * @throws InvalidOperationException if there are any existing travels for the specified driver that conflict with the new travel's
     *                                   time frame, as determined by the database query.
     */
    public void checkIfTheTravelTimeFrameIsValidWithQuery(Travel travel, User driver) {
        // Query the database to count the number of conflicting travels for the specified driver.
        int conflictingTravelCount = travelRepository.countConflictingTravels(
                driver.getId(),
                travel.getDepartureTime(),
                travel.getEstimatedTimeOfArrival()
        );

        // If any conflicting travels are found, throw an exception indicating an invalid time frame.
        if (conflictingTravelCount > 0) {
            throw new InvalidOperationException(EXISTING_TRAVEL);
        }
    }

    /**
     * Calculates the distance, duration, and estimated time of arrival for a travel based on departure and arrival locations.
     * <p>
     * This method calculates the distance and duration of a travel between the provided departure and arrival locations using a mapping service.
     * It also calculates the estimated time of arrival by adding the calculated duration to the departure time. The results are then
     * stored in the "travel" object.
     *
     * @param travel The travel for which distance, duration, and estimated time of arrival are being calculated.
     */
    public void calculatingDistanceAndDuration(Travel travel) {
        // Retrieve the location information and coordinates for the departure and arrival points.
        String departurePoint = bingMapsService.getLocationJson(travel.getDeparturePoint());
        double[] coordinatesOfDeparturePoint = bingMapsService.parseCoordinates(departurePoint);
        double departureLatitude = coordinatesOfDeparturePoint[0];
        double departureLongitude = coordinatesOfDeparturePoint[1];

        String arrivalPoint = bingMapsService.getLocationJson(travel.getArrivalPoint());
        double[] coordinatesOfArrivalPoint = bingMapsService.parseCoordinates(arrivalPoint);
        double arrivalLatitude = coordinatesOfArrivalPoint[0];
        double arrivalLongitude = coordinatesOfArrivalPoint[1];

        // Format the departure and arrival points into coordinates.
        String departurePointInCoordinates = String.format("%f,%f", departureLatitude, departureLongitude);
        String arrivalPointInCoordinates = String.format("%f,%f", arrivalLatitude, arrivalLongitude);

        // Extract the interval between duration and distance (e.g., "4.2 mi (7 mins)") to separate them.
        int intervalBetweenDurationAndDistance = bingMapsService.getTravelDistance(
                departurePointInCoordinates, arrivalPointInCoordinates).indexOf('m');

        // Set the travel duration and distance in the "travel" object.
        travel.setTravelDuration(bingMapsService.getTravelDistance(
                        departurePointInCoordinates, arrivalPointInCoordinates)
                .substring(intervalBetweenDurationAndDistance + 1));
        travel.setDistance(bingMapsService.getTravelDistance(departurePointInCoordinates, arrivalPointInCoordinates)
                .substring(0, intervalBetweenDurationAndDistance + 1));

        // Extract the minutes from the duration and calculate the estimated time of arrival.
        int indexOfMinutes = travel.getTravelDuration().indexOf('m');
        double minutesToAdd = Double.parseDouble(travel.getTravelDuration().substring(0, indexOfMinutes - 1));
        long secondsToAdd = (long) (minutesToAdd * 60);
        LocalDateTime arrivalTime = travel.getDepartureTime().plusSeconds(secondsToAdd);

        // Set the estimated time of arrival in the "travel" object.
        travel.setEstimatedTimeOfArrival(arrivalTime);
    }

    /**
     * Updates the status of active travels to "ACTIVE" based on their departure time.
     * This method is scheduled to run periodically.
     */
    @Override
    @Scheduled(fixedRate = 6000)
    public void updateTravelStatus() {
        List<Travel> plannedTravels = findPlannedTravelsWithPastDepartureTime();
        for (Travel travel : plannedTravels) {
            if (travel.getDepartureTime().isBefore(LocalDateTime.now())) {
                travel.setStatus(TravelStatus.ACTIVE);
                travelRepository.save(travel);
            }
        }
    }

    /**
     * Completes all active travels associated with the specified user as a driver.
     * This method marks all active travels of the user as a driver as "completed" and sets them as "deleted." Completed travels typically
     * indicate that the journey has concluded, and they may be flagged as deleted to indicate their historical status. After calling this
     * method, the active travels will have their status updated and be marked as deleted in the user's travel records.
     *
     * @param user The user for whom active travels are being completed and marked as deleted.
     */
    public static void completeActiveTravels(User user) {
        // Retrieve the list of travels driven by the specified user.
        List<Travel> travels = user.getTravelsAsDriver();

        // Filter and select only the active travels from the list.
        travels = travels.stream()
                .filter(travel -> travel.getStatus() == TravelStatus.ACTIVE)
                .collect(Collectors.toList());

        // Mark each active travel as "completed" and set them as "deleted."
        for (Travel travel : travels) {
            travel.setStatus(TravelStatus.COMPLETED);
            travel.setDeleted(true);
        }
    }
}

