package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TravelServiceImpl implements TravelService {

    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d is not existing!";
    public static final String USER_NOT_FOUND = "User with ID %d does not exist!";
    public static final String TRAVEL_REQUEST_NOT_FOUND = "Travel request with ID %d was not found!";
    private final TravelRepository travelRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final UserRepository userRepository;
    private final BingMapsService bingMapsService;

    public TravelServiceImpl(TravelRepository travelRepository, TravelRequestRepository travelRequestRepository, UserRepository userRepository, BingMapsService bingMapsService) {
        this.travelRepository = travelRepository;
        this.travelRequestRepository = travelRequestRepository;
        this.userRepository = userRepository;
        this.bingMapsService = bingMapsService;
    }


    public List<Travel> get() {
        return travelRepository.findAll();
    }

    @Override
    public Travel getById(Long id) {
        return travelRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id)));
    }

    @Override
    public List<Travel> findByCriteria(String driver, TravelStatus status, Short freeSpots, LocalDateTime departureTime, Sort sort) {
        return travelRepository.findByCriteria(driver, status, freeSpots, departureTime, sort);
    }

    public List<Travel> findAll(Sort sort) {
        return travelRepository.findAll(sort);
    }

    @Override
    public Long count() {
        return travelRepository.count();
    }

    /**
     * @param travel This parameter is needed to create the travel itself
     * @param driver This parameter is needed to set the driver of the travel automatically
     *               First departurePoint is extracted from the user's input with the help
     *               of Microsoft Bing Maps External API method.
     *               On the second row we are extracting the coordinates of the departure location
     *               again using external API endpoint and this is repeating for the arrival point.
     *
     *               When we have extracted coordinates for both the departure and arrival locations
     *               we can calculate the distance and the estimated time duration between them using
     *               Microsft Bing Maps API external endpoint for calculation.
     */
    @Override
    public void create(Travel travel , User driver) {
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
        travel.setDistance(bingMapsService.getTravelDistance(departurePointInCoordinates,arrivalPointInCoordinates));
        travel.setStatus(TravelStatus.ACTIVE);
        travel.setDriver(driver);
        travelRepository.save(travel);
    }

    @Override
    public void update(Long id) {
        Travel travel = getById(id);
        travelRepository.save(travel);
    }

    @Override
    public void delete(Long id) {
        travelRepository.delete(id);
    }

    @Override
    public void completeTravel(Long id) {

        travelRepository.completeTravel(id);
    }

    @Override
    public void cancelTravel(Long id) {
        travelRepository.delete(id);
    }

    @Override
    public TravelRequest createRequest(Travel travel, User user) {
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setTravel(travel);
        travelRequest.setUser(user);
        travelRequest.setStatus(TravelRequestStatus.PENDING);
        travelRequestRepository.save(travelRequest);
        return travelRequest;
    }

    @Override
    public void approveRequest(Long id) {
        TravelRequest request = travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
        request.setStatus(TravelRequestStatus.ACCEPTED);
        travelRequestRepository.save(request);
    }

    @Override
    public void rejectRequest(Long id) {
        TravelRequest request = travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
        request.setStatus(TravelRequestStatus.REJECTED);
        travelRequestRepository.save(request);
    }

    @Override
    public TravelRequest get(Long id) {
        return travelRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_REQUEST_NOT_FOUND, id)));
    }
}
