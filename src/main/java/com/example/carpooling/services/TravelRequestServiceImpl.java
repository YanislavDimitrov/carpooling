package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.services.contracts.TravelRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TravelRequestServiceImpl implements TravelRequestService {
    public static final String NOT_AUTHORIZED = "You are not authorized to update this request!";
    private final TravelRequestRepository travelRequestRepository;
    public static final String TRAVEL_REQUEST_NOT_FOUND = "Travel request with ID %d was not found!";

    @Autowired
    public TravelRequestServiceImpl(TravelRequestRepository travelRequestRepository) {
        this.travelRequestRepository = travelRequestRepository;
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
    public TravelRequest createRequest(Travel travel, User user) {
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setTravel(travel);
        travelRequest.setPassenger(user);
        travelRequest.setStatus(TravelRequestStatus.PENDING);
        travelRequestRepository.save(travelRequest);
        return travelRequest;
    }

    /**
     * @param id this parameter is used to identify if there is valid request with this ID and if there is one
     *           to approve the request of the user
     * @throws EntityNotFoundException if a travel request with this ID is not existing
     */
    @Override
    public void approveRequest(Long id) {
        TravelRequest request = travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
        request.setStatus(TravelRequestStatus.ACCEPTED);
        travelRequestRepository.save(request);
    }

    /**
     * @param id this parameter is used to identify if there is a travel request with this ID and if there is one
     *           to reject the request of the user
     * @throws EntityNotFoundException if a travel request with this ID is not existing
     */
    @Override
    public void rejectRequest(Long id) {
        TravelRequest request = travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
        request.setStatus(TravelRequestStatus.REJECTED);
        travelRequestRepository.save(request);
    }

    /**
     * @param travelRequest the updated entity which is given from the controller
     * @param editor the user who is trying to update the entity
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
     * @param id used to find the entity
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
