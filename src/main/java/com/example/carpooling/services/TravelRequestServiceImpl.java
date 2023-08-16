package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.DuplicateEntityException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.VehicleIsFullException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
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
    private final TravelRequestRepository travelRequestRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    public static final String TRAVEL_REQUEST_NOT_FOUND = "Travel request with ID %d was not found!";

    @Autowired
    public TravelRequestServiceImpl(TravelRequestRepository travelRequestRepository, TravelRepository travelRepository, UserRepository userRepository) {
        this.travelRequestRepository = travelRequestRepository;
        this.travelRepository = travelRepository;
        this.userRepository = userRepository;
    }

    /**
     * @return all entities from the database , if there are not any , returns empty list
     */
    @Override
    public List<TravelRequest> get() {
        return travelRequestRepository.findAll();
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
     * @param travel this parameter is used as a reference where we are doing the request for
     * @param user   this parameter is used to identify who is making the request for the travel
     * @return TravelRequest entity if the parameters are valid.
     */
    @Override
    public String createRequest(Travel travel, User user) {
        TravelRequest travelRequest = new TravelRequest();
        if(travel.getFreeSpots() == 0) {
            throw new VehicleIsFullException(VEHICLE_IS_FULL);
        }
        if(!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TravelServiceImpl.TRAVEL_NOT_FOUND,travel.getId()));
        }
        if(!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND,user.getId()));
        }
        Optional<TravelRequest> travelRequestOptional = travel
                .getTravelRequests()
                .stream()
                .filter(travelRequest1 -> travelRequest1.getPassenger().equals(user))
                .findFirst();
        if(travelRequestOptional.isPresent()) {
            throw new DuplicateEntityException(REQUEST_ALREADY_SENT);
        } else {
            travelRequest.setTravel(travel);
            travelRequest.setPassenger(user);
            travelRequest.setStatus(TravelRequestStatus.PENDING);
            travelRequestRepository.save(travelRequest);
            return String.format(SUCCESSFULL_REQUEST,
                    travel.getDeparturePoint(),
                    travel.getArrivalPoint(),
                    travel.getDepartureTime());
        }

    }

    /**
     * @param id this parameter is used to identify if there is valid request with this ID and if there is one
     *           to approve the request of the user
     * @throws EntityNotFoundException if a travel request with this ID is not existing
     */
    //ToDo add validation if the driver is approving the request
    @Override
    public void approveRequest(Long id, User editor) {
        TravelRequest request = travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
        if (request.getTravel().getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        request.setStatus(TravelRequestStatus.APPROVED);
        short freeSpots = (short) (request.getTravel().getFreeSpots() - 1);
        request.getTravel().setFreeSpots(freeSpots);
        if(request.getTravel().getFreeSpots() == 0 ) {
            throw new VehicleIsFullException(VEHICLE_IS_FULL);
        }
        travelRequestRepository.save(request);
    }

    /**
     * @param id this parameter is used to identify if there is a travel request with this ID and if there is one
     *           to reject the request of the user
     * @throws EntityNotFoundException if a travel request with this ID is not existing
     */
    @Override
    public void rejectRequest(Long id, User editor) {
        TravelRequest request = travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
        if (request.getTravel().getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        request.setStatus(TravelRequestStatus.REJECTED);
        travelRequestRepository.save(request);
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
}
