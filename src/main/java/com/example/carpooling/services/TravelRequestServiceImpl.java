package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.InvalidOperationException;
import com.example.carpooling.exceptions.VehicleIsFullException;
import com.example.carpooling.exceptions.duplicate.DuplicateEntityException;
import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.PassengerRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.TravelRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * This class implements the TravelRequestService interface and provides methods for managing travel requests.
 * It handles operations such as creating, approving, rejecting, and deleting travel requests.
 *
 * @author Ivan Boev & Yanislav Dimitrov
 * @version 1.0
 * @since 2023-09-06
 */
@Service
public class TravelRequestServiceImpl implements TravelRequestService {
    public static final String NOT_AUTHORIZED = "You are not authorized to update this request!";
    public static final String OPERATION_DENIED = "Only the driver of the travel can approve requests!";
    public static final String VEHICLE_IS_FULL = "There isn't free spot left in the vehicle";
    public static final String USER_NOT_FOUND = "User with ID %d was not found!";
    public static final String REQUEST_ALREADY_SENT = "You have already sent a request to participate in this travel!";
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String DRIVER_APPLYING_RESTRICTION = "You cannot apply to be passenger for your own travel!";
    public static final String TRAVEL_NOT_ACTIVE = "You cannot apply for travel which is not active!";
    public static final String INVALID_OPS = "You cannot remove a passenger if the travel is not in Planned status!";
    private final TravelRequestRepository travelRequestRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;

    public static final String TRAVEL_REQUEST_NOT_FOUND = "Travel request with ID %d was not found!";

    @Autowired
    public TravelRequestServiceImpl(TravelRequestRepository travelRequestRepository, TravelRepository travelRepository, UserRepository userRepository, PassengerRepository passengerRepository) {
        this.travelRequestRepository = travelRequestRepository;
        this.travelRepository = travelRepository;
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
    }

    /**
     * Retrieves all travel requests from the database.
     *
     * @return List of TravelRequest entities, or an empty list if none are found.
     */
    @Override
    public List<TravelRequest> get() {
        return travelRequestRepository.findAll();
    }

    /**
     * Retrieves a list of pending travel requests from the database.
     *
     * @return A List of TravelRequest entities with a status of PENDING.
     */
    @Override
    public List<TravelRequest> getPending() {
        return travelRequestRepository.findAllByStatusIs(TravelRequestStatus.PENDING);
    }

    /**
     * Retrieves a list of travel requests associated with a specific travel.
     *
     * @param travel The Travel entity for which travel requests are to be retrieved.
     * @return A List of TravelRequest entities associated with the specified travel.
     * @throws EntityNotFoundException if the provided travel ID does not exist in the database.
     */
    @Override
    public List<TravelRequest> getByTravel(Travel travel) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        return travelRequestRepository.findByTravelIs(travel);
    }

    /**
     * Finds a specific travel request by travel, passenger, and status.
     *
     * @param travel    The Travel entity associated with the travel request.
     * @param passenger The User entity associated with the passenger making the request.
     * @param status    The status of the travel request to be found (e.g., PENDING, APPROVED).
     * @return The TravelRequest entity matching the provided parameters.
     */
    @Override
    public TravelRequest findByTravelIsAndPassengerIsAndStatus(Travel travel, User passenger, TravelRequestStatus status) {
        return travelRequestRepository.findByTravelIsAndPassengerIsAndStatus(travel, passenger, status);
    }

    /**
     * @param id this parameter is used to identify if there is a travel reques with this id and if there is
     *           to return its value
     * @return TravelRequest entity
     * @throws EntityNotFoundException if a travel request with this ID is not existing.
     */
    @Override
    public TravelRequest get(Long id) {
        return travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
    }

    /**
     * Retrieves a list of travel requests associated with a specific travel and matching the specified status.
     *
     * @param travel The Travel entity for which travel requests are to be retrieved.
     * @param status The desired status of the travel requests to be retrieved (e.g., PENDING, APPROVED).
     * @return A List of TravelRequest entities associated with the specified travel and matching the given status.
     * @throws EntityNotFoundException if the provided travel ID does not exist in the database.
     */
    @Override
    public List<TravelRequest> findByTravelIsAndStatus(Travel travel, TravelRequestStatus status) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        return travelRequestRepository.findByTravelIsAndStatus(travel, TravelRequestStatus.PENDING);
    }

    /**
     * Creates a travel request for a specified travel and user.
     *
     * @param travel The travel for which the request is made.
     * @param user   The user making the request.
     * @throws VehicleIsFullException    if the travel's vehicle is already full.
     * @throws EntityNotFoundException   if the travel or user is not found in the database.
     * @throws InvalidOperationException if the user is the driver of the travel or if the travel is not in a planned status.
     * @throws DuplicateEntityException  if the user has already sent a request for this travel.
     */
    @Override
    public void createRequest(Travel travel, User user) {
        TravelRequest travelRequest = new TravelRequest();
        if (travel.getFreeSpots() == 0) {
            throw new VehicleIsFullException(VEHICLE_IS_FULL);
        }
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TravelServiceImpl.TRAVEL_NOT_FOUND, travel.getId()));
        }
        if (!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, user.getId()));
        }
        if (travel.getDriver() == user) {
            throw new InvalidOperationException(DRIVER_APPLYING_RESTRICTION);
        }
        if (travel.getStatus() != TravelStatus.PLANNED) {
            throw new InvalidOperationException(TRAVEL_NOT_ACTIVE);
        }
        Optional<TravelRequest> travelRequestOptional = travel
                .getTravelRequests()
                .stream()
                .filter(travelRequest1 -> travelRequest1.getPassenger().equals(user) && travelRequest1.getStatus() == TravelRequestStatus.PENDING || travelRequest1.getPassenger().equals(user) && travelRequest1.getStatus() == TravelRequestStatus.APPROVED)
                .findFirst();

        if (travelRequestOptional.isPresent()) {
            throw new DuplicateEntityException(REQUEST_ALREADY_SENT);
        } else {
            travelRequest.setTravel(travel);
            travelRequest.setPassenger(user);
            travelRequest.setStatus(TravelRequestStatus.PENDING);
            travelRequestRepository.save(travelRequest);
        }
    }

    /**
     * Approves a travel request for the specified travel, editor, and request creator. The method checks if the editor is the driver of the travel,
     * if there are available spots in the vehicle, and if the necessary attributes exist. If all conditions are met, the request is approved,
     * and a new passenger is added to the travel.
     *
     * @param travel         The travel for which the request is being approved.
     * @param editor         The user who is approving the request (driver of the travel).
     * @param requestCreator The user who created the travel request.
     * @throws AuthorizationException If the editor is not the driver of the travel.
     * @throws VehicleIsFullException If there are no available spots in the vehicle.
     */
    @Override
    @Transactional
    public void approveRequest(Travel travel, User editor, User requestCreator) {
        TravelRequest request = travelRequestRepository.findByTravelIsAndPassengerIsAndStatus(travel, requestCreator, TravelRequestStatus.PENDING);
        if (request.getTravel().getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        if (request.getTravel().getFreeSpots() == 0) {
            throw new VehicleIsFullException(VEHICLE_IS_FULL);
        }
        checkIfAttributesExists(travel, editor, requestCreator);
        request.setStatus(TravelRequestStatus.APPROVED);
        short freeSpots = (short) (request.getTravel().getFreeSpots() - 1);
        request.getTravel().setFreeSpots(freeSpots);
        travelRequestRepository.save(request);

        Passenger passenger = new Passenger();
        passenger.setUser(request.getPassenger());
        passenger.setTravel(request.getTravel());
        passengerRepository.save(passenger);
    }

    /**
     * Rejects a travel request for the specified travel, editor, and request creator. The method checks if the editor is the driver of the travel
     * and if the necessary attributes exist. If all conditions are met, the request is rejected.
     *
     * @param travel         The travel for which the request is being rejected.
     * @param editor         The user who is rejecting the request (driver of the travel).
     * @param requestCreator The user who created the travel request.
     * @throws AuthorizationException If the editor is not the driver of the travel.
     */
    @Override
    public void rejectRequest(Travel travel, User editor, User requestCreator) {
        checkIfAttributesExists(travel, editor, requestCreator);
        if (!travel.getDriver().equals(editor)) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        TravelRequest request = travelRequestRepository.findByTravelIsAndPassengerIsAndStatus(travel,
                requestCreator,
                TravelRequestStatus.PENDING);
        request.setStatus(TravelRequestStatus.REJECTED);
        travelRequestRepository.save(request);


    }

    /**
     * Rejects a travel request for the specified travel and user when the user is already a passenger. The method checks if the editor is the driver
     * of the travel, if the travel status is 'PLANNED,' and if the necessary attributes exist. If all conditions are met, the request is rejected,
     * and the passenger is removed from the travel.
     *
     * @param travel The travel for which the request is being rejected.
     * @param user   The user whose request is being rejected (already a passenger).
     * @param editor The user who is rejecting the request (driver of the travel).
     * @throws AuthorizationException    If the editor is not the driver of the travel.
     * @throws InvalidOperationException If the travel status is not 'PLANNED.'
     */
    @Override
    @Transactional
    public void rejectRequestWhenUserIsAlreadyPassenger(Travel travel, User user, User editor) {
        checkIfAttributesExists(travel, editor, user);
        if (!travel.getDriver().equals(editor)) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        if (travel.getStatus() != TravelStatus.PLANNED) {
            throw new InvalidOperationException(INVALID_OPS);
        }
        TravelRequest travelRequest = findByTravelIsAndPassengerIsAndStatus(travel, user, TravelRequestStatus.APPROVED);
        travelRequest.setStatus(TravelRequestStatus.PENDING);

        Optional<Passenger> passenger = passengerRepository.findByUserAndTravel(user, travel);
        if (passenger.isPresent()) {
            passengerRepository.delete(passenger.get());
            travel.setFreeSpots((short) (travel.getFreeSpots() + 1));
        }
    }

    /**
     * Helper method to check if the necessary attributes (travel, editor, and requestCreator) exist and if they are valid.
     * This method is used in other methods to perform attribute existence and validity checks.
     *
     * @param travel         The travel for which the attributes are being checked.
     * @param editor         The editor (user) whose attributes are being checked.
     * @param requestCreator The request creator (user) whose attributes are being checked.
     * @throws EntityNotFoundException If the travel, editor, or requestCreator is not found.
     * @throws AuthorizationException  If there are authorization issues with the provided attributes.
     */
    private void checkIfAttributesExists(Travel travel, User editor, User requestCreator) {
        FeedbackServiceImpl.checkIfTravelAndUsersExist(travel, editor, requestCreator, travelRepository, TRAVEL_NOT_FOUND, userRepository, USER_NOT_FOUND);
    }

    /**
     * Updates a travel request entity in the system. This method is used to modify an existing travel request
     * and save the changes. It performs an authorization check to ensure that the editor is the passenger
     * associated with the travel request.
     *
     * @param travelRequest The travel request entity to be updated.
     * @param editor        The user who is editing the travel request.
     * @throws AuthorizationException If the editor is not authorized to update the travel request.
     */
    @Override
    public void update(TravelRequest travelRequest, User editor) {
        if (travelRequest.getPassenger() != editor) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        travelRequestRepository.save(travelRequest);
    }

    /**
     * Deletes a travel request entity from the system using its unique identifier. This method requires the editor
     * to be the creator of the travel request and performs an authorization check accordingly.
     *
     * @param id     The unique identifier of the travel request entity to be deleted.
     * @param editor The user who is trying to delete the travel request.
     * @throws AuthorizationException If the editor is not authorized to delete the travel request.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id, User editor) {
        if (get(id).getPassenger() != editor) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        travelRequestRepository.delete(get(id));
    }

    /**
     * Deletes a travel request associated with a specific travel and passenger. This method checks the existence of
     * the provided travel and user entities and performs necessary deletions if they exist. It also increases the
     * number of free spots in the associated travel if the deletion is successful.
     *
     * @param travel The travel entity for which the travel request is associated.
     * @param user   The user entity whose travel request is to be deleted.
     * @throws EntityNotFoundException If the provided travel or user entities are not found in the system.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByTravelAndPassenger(Travel travel, User user) {
        Optional<Passenger> passenger = passengerRepository.findByUserAndTravel(user, travel);

        travelRequestRepository.deleteByTravelAndPassenger(travel, user);

        if (passenger.isPresent()) {
            passengerRepository.delete(passenger.get());
            travel.setFreeSpots((short) (travel.getFreeSpots() + 1));
        }
    }

    /**
     * Rejects a travel request when the user is already a passenger on the specified travel and the editor is the driver of the travel.
     * This method checks the existence of the user, editor, and travel entities, and verifies if the user is a passenger on the travel.
     * If all conditions are met, the associated travel request is rejected.
     *
     * @param id     The unique identifier of the user whose travel request is to be rejected.
     * @param editor The user who is trying to reject the travel request (must be the driver of the travel).
     * @param travel The travel for which the travel request is being rejected.
     * @throws EntityNotFoundException   If the provided user, editor, or travel entities are not found in the system.
     * @throws InvalidOperationException If the editor is not the driver of the travel or if the operation is invalid.
     */
    @Override
    public void rejectWhenAlreadyPassenger(Long id, User editor, Travel travel) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, id));
        }
        if (!userRepository.existsById(editor.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, editor.getId()));
        }
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        Optional<User> user = userRepository.findById(id);
        if (haveTravelInTheList(user.get(), travel) && travel.getDriver().equals(editor)) {
            TravelRequest travelRequest = travelRequestRepository.findByTravelIs(travel).stream().filter(travelRequest1 -> travelRequest1.getPassenger().equals(user)).findFirst().get();
            travelRequest.setStatus(TravelRequestStatus.REJECTED);
        } else {
            throw new InvalidOperationException("Invalid operation!You cannot reject travel request if you are not the driver!");
        }
    }

    /**
     * Checks if a user has a specific travel in their approved travel requests.
     *
     * @param user   The user to check.
     * @param travel The travel to check for.
     * @return true if the user has the travel in their approved requests, false otherwise.
     */
    public boolean haveTravelInTheList(User user, Travel travel) {
        for (TravelRequest travelRequest : user.getTravelsAsPassenger()) {
            if (travelRequest.getTravel().equals(travel) && travelRequest.getStatus() == TravelRequestStatus.APPROVED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a travel request with a specific status exists for the given travel and passenger combination.
     *
     * @param travel The travel for which the existence of a travel request is being checked.
     * @param user   The passenger (user) for whom the existence of a travel request is being checked.
     * @param status The status of the travel request to check for.
     * @return {@code true} if a travel request with the specified status exists for the given travel and passenger, {@code false} otherwise.
     */
    @Override
    public boolean existsTravelRequestByTravelAndPassengerAndStatus(Travel travel, User user, TravelRequestStatus status) {
        return travelRequestRepository.existsTravelRequestByTravelAndPassengerAndStatus(travel, user, status);
    }

    /**
     * Checks if there exists any travel request associated with a specific travel and passenger.
     *
     * @param travel The travel for which the existence of a travel request is being checked.
     * @param user   The passenger (user) for whom the existence of a travel request is being checked.
     * @return {@code true} if there is a travel request associated with the specified travel and passenger, {@code false} otherwise.
     */
    @Override
    public boolean existsByTravelAndPassenger(Travel travel, User user) {
        return travelRequestRepository.existsByTravelAndPassenger(travel, user);
    }
}
