package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TravelServiceImpl implements TravelService {

    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d is not existing!";
    public static final String USER_NOT_FOUND = "User with ID %d does not exist!";

    public static final String UPDATE_CANCELLED = "You cannot update this travel!";
    public static final String OPERATION_DENIED = "You are not authorized to complete this operation!";
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


    /**
     * @return Returns all travels which are in the database using JPA Repository findAll method
     */
    public List<Travel> get() {
        return travelRepository.findAll();
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
     * @param sort – the Sort specification to sort the results by, can be Sort.unsorted(), must not be null.
     * @return all entities sorted by the given options
     */
    public List<Travel> findAll(Sort sort) {
        return travelRepository.findAll(sort);
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
     *               With the help of external Microsoft Bing Maps API endpoints , a new travel has been created.
     */
    @Override
    public void create(Travel travel, User driver) {
        calculatingDistanceAndDuration(travel);
        travel.setStatus(TravelStatus.ACTIVE);
        travel.setDriver(driver);
        travelRepository.save(travel);
    }


    /**
     * @param travel this parameter is used for extraction of departure address of the travel and arrival address
     *               of the travel and then via methods of Microsoft Bing Maps these addresses are converted to
     *               coordinates and are held to other external API endpoint which calculates the distance and the
     *               estimated time of travelling between the two places the user typed in the input field.
     */
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

    /**
     * @param travel this parameter is the travel which was held by the controller class and it is with already refactored
     *               fields so this is the new travel to be persisted in the database
     * @param editor this parameters refers to the person who is trying to update the travel
     * @return updated Travel
     * @throws AuthorizationException if the editor is not the driver of the travel.
     */
    @Override
    public Travel update(Travel travel, User editor) {
        if (travel.getDriver() != editor) {
            throw new AuthorizationException(UPDATE_CANCELLED);
        }
        if(!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND,travel.getId()));
        }
        calculatingDistanceAndDuration(travel);
        travelRepository.save(travel);
        return travel;
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
        if(!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND,id));
        }

        travelRepository.delete(id);
    }

    /**
     * @param id this parameters is used to find a travel with this ID
     *           This method is changing the status of a certain travel with status 'COMPLETED'
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Travel completeTravel(Long id , User editor) {
        if (!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id));
        }
        Travel travel = getById(id);
        if(travel.getDriver() != editor) {
            throw new AuthorizationException(OPERATION_DENIED);
        }
        travelRepository.completeTravel(id);
        return travel;
    }

    /**
     * @param id this parameter is used to find a travel with this ID
     *           This method is used to change the status of a certain travel with status 'DELETED'
     */
    @Override
    public void cancelTravel(Long id) {
        if(!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND,id));
        }
        travelRepository.delete(id);
    }

}
