package com.example.carpooling.services;

import com.example.carpooling.exceptions.*;
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

@Service
public class TravelRequestServiceImpl implements TravelRequestService {
    public static final String NOT_AUTHORIZED = "You are not authorized to update this request!";
    public static final String OPERATION_DENIED = "Only the driver of the travel can approve requests!";
    public static final String VEHICLE_IS_FULL = "There isn't free spot left in the vehicle";
    public static final String USER_NOT_FOUND = "User with ID %d was not found!";
    public static final String SUCCESSFULL_REQUEST = "Your request for travel from %s to %s on %s was created successfully!";
    public static final String REQUEST_ALREADY_SENT = "You have already sent a request to participate in this travel!";
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String DRIVER_APPLYING_RESTRICTION = "You cannot apply to be passenger for your own travel!";
    public static final String TRAVEL_NOT_ACTIVE = "You cannot apply for travel which is not active!";
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
     * @return all entities from the database , if there are not any , returns empty list
     */
    @Override
    public List<TravelRequest> get() {
        return travelRequestRepository.findAll();
    }

    @Override
    public List<TravelRequest> getPending() {
        return travelRequestRepository.findAllByStatusIs(TravelRequestStatus.PENDING);
    }

    @Override
    public List<TravelRequest> getByTravel(Travel travel) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        return travelRequestRepository.findByTravelIs(travel);
    }

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

    @Override
    public List<TravelRequest> findByTravelIsAndStatus(Travel travel, TravelRequestStatus status) {
        if(!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND,travel.getId()));
        }
        return travelRequestRepository.findByTravelIsAndStatus(travel,TravelRequestStatus.PENDING);
    }

    /**
     * @param travel this parameter is used as a reference where we are doing the request for
     * @param user   this parameter is used to identify who is making the request for the travel
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

    @Override
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

    @Override
    public void rejectRequestWhenUserIsAlreadyPassenger(Travel travel, User user, User editor) {
        checkIfAttributesExists(travel, editor, user);
        if (!travel.getDriver().equals(editor)) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        if (travel.getStatus() != TravelStatus.PLANNED) {
            throw new InvalidOperationException("You cannot remove a passenger if the travel is not in Planned status!");
        }
        TravelRequest travelRequest = findByTravelIsAndPassengerIsAndStatus(travel, user, TravelRequestStatus.APPROVED);
        travelRequest.setStatus(TravelRequestStatus.REJECTED);
        travelRequestRepository.delete(travelRequest);

        travel.setFreeSpots((short) (travel.getFreeSpots() + 1));

        Passenger passenger = passengerRepository.findByUserAndTravel(user, travel);
        passengerRepository.delete(passenger);
    }

    private void checkIfAttributesExists(Travel travel, User editor, User requestCreator) {
        FeedbackServiceImpl.checkIfTravelAndUsersExist(travel, editor, requestCreator, travelRepository, TRAVEL_NOT_FOUND, userRepository, USER_NOT_FOUND);
    }

    /**
     * @param travelRequest the updated entity which is given from the controller
     * @param editor        the user who is trying to update the entity
     * @throws AuthorizationException if the user is not the creator of the travel
     */
    @Override
    public void update(TravelRequest travelRequest, User editor) {
        if (travelRequest.getPassenger() != editor) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        travelRequestRepository.save(travelRequest);
    }

    /**
     * @param id     used to find the entity
     * @param editor used to identify who is trying to delete this entity
     * @throws AuthorizationException if the editor is not the creator of the creator of the entity
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id, User editor) {
        if (get(id).getPassenger() != editor) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        travelRequestRepository.delete(get(id));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByTravelAndAndPassenger(Travel travel, User user) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        if (!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, user.getId()));
        }
        boolean isPassenger = passengerRepository.existsByUserAndTravel(user, travel);
        Passenger passenger = passengerRepository.findByUserAndTravel(user, travel);
        if (isPassenger) {
            travelRequestRepository.deleteByTravelAndAndPassenger(travel, user);
        }
        passengerRepository.delete(passenger);
        travel.setFreeSpots((short) (travel.getFreeSpots() + 1));
    }

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

    public boolean haveTravelInTheList(User user, Travel travel) {
        for (TravelRequest travelRequest : user.getTravelsAsPassenger()) {
            if (travelRequest.getTravel().equals(travel) && travelRequest.getStatus() == TravelRequestStatus.APPROVED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsTravelRequestByTravelAndPassengerAndStatus(Travel travel, User user, TravelRequestStatus status) {
        return travelRequestRepository.existsTravelRequestByTravelAndPassengerAndStatus(travel, user, status);
    }

    @Override
    public boolean existsByTravelAndPassenger(Travel travel, User user) {
        return travelRequestRepository.existsByTravelAndPassenger(travel, user);
    }
}
